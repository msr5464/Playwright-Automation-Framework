package automation.modules.aiteststudio;

import automation.core.Config;
import automation.core.Log;
import automation.core.ai.CostEvaluator;
import automation.core.ai.EvalCase;
import automation.core.ai.EvalResponse;
import automation.core.ai.Evaluator;
import automation.core.ai.LatencyEvaluator;
import automation.core.ai.MustContainEvaluator;
import automation.core.ai.MustNotContainEvaluator;
import automation.core.api.ApiHelper;
import automation.modules.aiteststudio.api.AiTestStudioApi;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Adapter for AI-Test-Studio: logs in, calls its AI features and maps each reply into an {@link EvalResponse}.
 * Needs {@code aiteststudio.username} and {@code aiteststudio.password} in parameters/system.properties.
 */
public class AiTestStudioHelper extends ApiHelper
{
    private String sessionCookie;

    public AiTestStudioHelper(Config config)
    {
        super(config, config.getRunTimeProperty("aiteststudio.api.url"));
    }

    public void login()
    {
        String username = config.getRunTimeProperty("aiteststudio.username");
        String password = config.getRunTimeProperty("aiteststudio.password");
        if (username == null || password == null)
        {
            config.logFailToEndExecution("Add aiteststudio.username and aiteststudio.password to parameters/system.properties");
        }
        Log.comment(config, "Logging in to AI-Test-Studio as " + username);
        // post(), not execute(): execute logs the request body, which would put the password in the report
        Response response = post(AiTestStudioApi.Login.getEndpoint(),
            map().put("username", username).put("password", password).build());
        if (response.getStatusCode() != AiTestStudioApi.Login.getExpectedStatus())
        {
            config.logFailToEndExecution("Login to AI-Test-Studio failed: HTTP " + response.getStatusCode());
        }
        sessionCookie = "session=" + response.getCookie("session");
    }

    /** Asks Talk to Tests the case's question, bypassing Studio's answer cache so reruns reach the model. */
    public EvalResponse askTalkToTests(EvalCase evalCase)
    {
        TalkToTestsData question = new TalkToTestsData();
        question.setQuestion(evalCase.getInput());
        question.setUseRag(true);
        question.setBypassCache(true);

        long start = System.currentTimeMillis();
        // Sent per request rather than as a default header, so the session cookie stays out of the logged cURL
        Response response = executeRaw(AiTestStudioApi.Query, question, Map.of("Cookie", sessionCookie));
        long latencyMs = System.currentTimeMillis() - start;
        return toEvalResponse(evalCase, response.getStatusCode(), response.getBody().asString(), latencyMs);
    }

    /**
     * Maps a Talk to Tests reply into the shared shape. Unless Studio's show_matching_sources setting is on,
     * source_documents holds only the documents the answer cited, not everything retrieved.
     */
    public EvalResponse toEvalResponse(EvalCase evalCase, int statusCode, String body, long latencyMs)
    {
        EvalResponse.EvalResponseBuilder evalResponse = EvalResponse.builder()
            .caseId(evalCase.getCaseId())
            .latencyMs(latencyMs)
            .rawResponse(body);

        TalkToTestsData reply;
        try
        {
            reply = getObjectMapper().readValue(body, TalkToTestsData.class);
        }
        catch (IOException e)
        {
            return evalResponse.success(false).errorMessage("HTTP " + statusCode + ", unreadable reply").build();
        }

        boolean success = statusCode == AiTestStudioApi.Query.getExpectedStatus() && Boolean.TRUE.equals(reply.getSuccess());
        List<Map<String, Object>> sourceDocuments = reply.getSourceDocuments() == null ? List.of() : reply.getSourceDocuments();
        Object cost = reply.getMetrics() == null ? null : reply.getMetrics().get("cost_usd");
        return evalResponse
            .success(success)
            .errorMessage(success ? null : (reply.getError() != null ? reply.getError() : "HTTP " + statusCode))
            .output(reply.getAnswer())
            .retrievedContexts(sourceDocuments.stream().map(document -> String.valueOf(document.get("content"))).toList())
            .costUsd(cost instanceof Number number ? number.doubleValue() : 0.0)
            .build();
    }

    /** The checks every Talk to Tests case gets. Budgets: aiteststudio.maxLatencyMs, aiteststudio.maxCostUsd. */
    public List<Evaluator> talkToTestsEvaluators()
    {
        return List.of(
            new MustContainEvaluator(),
            new MustNotContainEvaluator(),
            new LatencyEvaluator(Long.parseLong(config.getRunTimeProperty("aiteststudio.maxLatencyMs", "10000"))),
            new CostEvaluator(Double.parseDouble(config.getRunTimeProperty("aiteststudio.maxCostUsd", "0.01"))));
    }
}
