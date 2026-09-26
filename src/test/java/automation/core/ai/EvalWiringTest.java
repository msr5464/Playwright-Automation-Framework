package automation.core.ai;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestVariables;
import org.testng.annotations.Test;

import java.util.List;

public class EvalWiringTest extends EvalTestBase
{
    @Test(dataProvider = "evalCases", groups = {GROUP_REGRESSION, GROUP_API},
          description = "Every dataset case reaches the test with its own Config and is scored by EvalAssert")
    @EvalDataset(module = "core", name = "wiring")
    @TestVariables(automatedBy = QA.Mukesh)
    public void datasetCasesFlowThroughEvalAssert(Config config, EvalCase evalCase)
    {
        AssertHelper.assertContains(config, config.testcaseName, evalCase.getCaseId(), "Each case runs with its own Config");
        EvalAssert.meets(config, evalCase, echo(evalCase), List.of(new MustContainEvaluator(), new MustNotContainEvaluator()));
    }

    // Stands in for an app adapter: answers by repeating the question
    private EvalResponse echo(EvalCase evalCase)
    {
        return EvalResponse.builder()
            .caseId(evalCase.getCaseId())
            .output("You asked: " + evalCase.getInput())
            .success(true)
            .build();
    }
}
