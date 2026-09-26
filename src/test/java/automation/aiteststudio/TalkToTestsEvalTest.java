package automation.aiteststudio;

import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestVariables;
import automation.core.ai.EvalAssert;
import automation.core.ai.EvalCase;
import automation.core.ai.EvalDataset;
import automation.core.ai.EvalResponse;
import automation.core.ai.EvalTestBase;
import automation.modules.aiteststudio.AiTestStudioHelper;
import org.testng.annotations.Test;

/**
 * Evaluates Talk to Tests against every case in src/test/resources/aiteststudio/evalCases/talk-to-tests/.
 * Needs a running AI-Test-Studio (aiteststudio.api.url) and its login in parameters/system.properties.
 */
public class TalkToTestsEvalTest extends EvalTestBase
{
    @Test(dataProvider = "evalCases", groups = {GROUP_REGRESSION, GROUP_API},
          description = "Talk to Tests answers each dataset question within its quality, latency and cost budgets")
    @EvalDataset(module = "aiteststudio", name = "talk-to-tests")
    @TestVariables(automatedBy = QA.Mukesh)
    public void answersDatasetQuestions(Config config, EvalCase evalCase)
    {
        AiTestStudioHelper studio = new AiTestStudioHelper(config);

        config.logStep("Log in to AI-Test-Studio");
        studio.login();

        config.logStep("Ask Talk to Tests: " + evalCase.getInput());
        EvalResponse response = studio.askTalkToTests(evalCase);

        config.logStep("Score the answer");
        EvalAssert.meets(config, evalCase, response, studio.talkToTestsEvaluators());
    }
}
