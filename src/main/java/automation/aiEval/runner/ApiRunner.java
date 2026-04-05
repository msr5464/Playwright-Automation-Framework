package automation.aiEval.runner;

import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.RunContext;
import automation.aiEval.normalizer.DefaultResponseNormalizer;
import automation.aiEval.normalizer.ResponseNormalizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

public class ApiRunner implements AIRunner
{
    private final ObjectMapper mapper = new ObjectMapper();
    private final ResponseNormalizer normalizer;

    public ApiRunner()
    {
        this.normalizer = new DefaultResponseNormalizer();
    }

    public ApiRunner(ResponseNormalizer normalizer)
    {
        this.normalizer = normalizer;
    }

    @Override
    public AIExecutionResult execute(AITestCase testCase, RunContext runContext)
    {
        String targetWorkflow = (testCase.getExecutionHints() != null
            && testCase.getExecutionHints().getTargetWorkflow() != null)
            ? testCase.getExecutionHints().getTargetWorkflow()
            : "evaluate";

        String endpoint = runContext.getBaseUrl() + "/" + targetWorkflow;

        Object requestBody = null;
        try
        {
            requestBody = testCase.getInput();
            String requestJson = mapper.writeValueAsString(requestBody);

            RequestSpecification spec = RestAssured.given()
                .contentType("application/json")
                .body(requestJson);

            if (runContext.getAuthToken() != null)
            {
                spec = spec.header("Authorization", "Bearer " + runContext.getAuthToken());
            }

            long start = System.currentTimeMillis();
            Response response = spec.post(endpoint);
            long latencyMs = System.currentTimeMillis() - start;

            Map<String, String> headers = new HashMap<>();
            response.getHeaders().forEach(h -> headers.put(h.getName(), h.getValue()));

            RawServiceResponse raw = RawServiceResponse.builder()
                .httpStatus(response.getStatusCode())
                .headers(headers)
                .body(response.getBody().asString())
                .latencyMs(latencyMs)
                .requestPayload(requestBody)
                .build();

            return normalizer.normalize(raw, testCase, runContext);
        }
        catch (Exception e)
        {
            return AIExecutionResult.builder()
                .caseId(testCase.getCaseId())
                .success(false)
                .errorType(e.getClass().getName())
                .errorMessage(e.getMessage())
                .requestPayload(requestBody)
                .timestamp(java.time.Instant.now().toString())
                .build();
        }
    }
}
