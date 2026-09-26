package automation.aiteststudio;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.ai.EvalCase;
import automation.core.ai.EvalResponse;
import automation.modules.aiteststudio.AiTestStudioHelper;
import org.testng.annotations.Test;

/**
 * How Talk to Tests replies map into EvalResponse. Uses recorded reply bodies, so it runs without Studio.
 */
public class TalkToTestsMappingTest extends TestBase
{
    private final EvalCase evalCase = EvalCase.builder().caseId("TTT-001").input("Which tests cover login?").build();

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void successfulReply_mapsAnswerCitedDocumentsAndCost(Config config)
    {
        String body = "{\"success\": true, \"answer\": \"C101 and C102 cover login.\", \"mode\": \"rag\","
            + " \"source_documents\": [{\"source_id\": 1, \"content\": \"C101 Login with a valid user\", \"metadata\": {}}],"
            + " \"metrics\": {\"cost_usd\": 0.0012, \"calls\": 1}}";

        EvalResponse response = new AiTestStudioHelper(config).toEvalResponse(evalCase, 200, body, 1500);

        AssertHelper.assertTrue(config, response.isSuccess(), "Reply is marked successful");
        AssertHelper.assertEquals(config, response.getOutput(), "C101 and C102 cover login.", "Answer");
        AssertHelper.assertEquals(config, response.getRetrievedContexts().get(0), "C101 Login with a valid user", "Cited document");
        AssertHelper.assertEquals(config, response.getCostUsd(), 0.0012, "Cost");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void errorReply_marksCallFailed(Config config)
    {
        EvalResponse response = new AiTestStudioHelper(config)
            .toEvalResponse(evalCase, 500, "{\"success\": false, \"error\": \"LLM timeout\"}", 30000);

        AssertHelper.assertFalse(config, response.isSuccess(), "Error reply is marked failed");
        AssertHelper.assertEquals(config, response.getErrorMessage(), "LLM timeout", "Error message");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void unreadableReply_marksCallFailed(Config config)
    {
        EvalResponse response = new AiTestStudioHelper(config).toEvalResponse(evalCase, 502, "<html>Bad Gateway</html>", 100);

        AssertHelper.assertFalse(config, response.isSuccess(), "Unreadable reply is marked failed");
        AssertHelper.assertContains(config, response.getErrorMessage(), "HTTP 502", "Error names the HTTP status");
    }
}
