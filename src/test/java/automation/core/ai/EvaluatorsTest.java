package automation.core.ai;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestBase;
import automation.core.TestVariables;
import org.testng.annotations.Test;

import java.util.List;

public class EvaluatorsTest extends TestBase
{
    private EvalCase evalCase(List<String> mustContain, List<String> mustNotContain)
    {
        return EvalCase.builder()
            .caseId("CASE-001")
            .input("question")
            .mustContain(mustContain)
            .mustNotContain(mustNotContain)
            .build();
    }

    private EvalResponse response(String output, long latencyMs)
    {
        return EvalResponse.builder().caseId("CASE-001").output(output).latencyMs(latencyMs).success(true).build();
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void mustContain_scoresShareOfPhrasesFound(Config config)
    {
        EvaluationResult result = new MustContainEvaluator()
            .evaluate(evalCase(List.of("30 days", "receipt"), null), response("You have 30 days to return an item.", 100));

        AssertHelper.assertEquals(config, result.getScore(), 0.5, "Score with one of two phrases found");
        AssertHelper.assertFalse(config, result.isPassed(), "A missing phrase fails the evaluator");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void mustContain_ignoresCase(Config config)
    {
        EvaluationResult result = new MustContainEvaluator()
            .evaluate(evalCase(List.of("RECEIPT"), null), response("Bring your receipt.", 100));

        AssertHelper.assertTrue(config, result.isPassed(), "Phrase matches regardless of case");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void mustContain_nothingRequired_passes(Config config)
    {
        EvaluationResult result = new MustContainEvaluator().evaluate(evalCase(null, null), response("anything", 100));

        AssertHelper.assertEquals(config, result.getScore(), 1.0, "Score with no required phrases");
        AssertHelper.assertTrue(config, result.isPassed(), "No required phrases passes the evaluator");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void mustNotContain_forbiddenPhraseFound_fails(Config config)
    {
        EvaluationResult result = new MustNotContainEvaluator()
            .evaluate(evalCase(null, List.of("system prompt")), response("My System Prompt says to be helpful.", 100));

        AssertHelper.assertEquals(config, result.getScore(), 0.0, "Score when a forbidden phrase appears");
        AssertHelper.assertFalse(config, result.isPassed(), "A forbidden phrase fails the evaluator");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void mustNotContain_forbiddenPhraseAbsent_passes(Config config)
    {
        EvaluationResult result = new MustNotContainEvaluator()
            .evaluate(evalCase(null, List.of("system prompt")), response("I can't share that.", 100));

        AssertHelper.assertTrue(config, result.isPassed(), "No forbidden phrase passes the evaluator");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void latency_withinLimit_scoresOne(Config config)
    {
        EvaluationResult result = new LatencyEvaluator(1000).evaluate(evalCase(null, null), response("answer", 800));

        AssertHelper.assertEquals(config, result.getScore(), 1.0, "Score within the latency limit");
        AssertHelper.assertTrue(config, result.isPassed(), "Within the limit passes the evaluator");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void latency_overLimit_scoresPartially(Config config)
    {
        EvaluationResult result = new LatencyEvaluator(1000).evaluate(evalCase(null, null), response("answer", 1500));

        AssertHelper.assertEquals(config, result.getScore(), 0.5, "Score halfway between the limit and twice the limit");
        AssertHelper.assertFalse(config, result.isPassed(), "Over the limit fails the evaluator");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void latency_overTwiceLimit_scoresZero(Config config)
    {
        EvaluationResult result = new LatencyEvaluator(1000).evaluate(evalCase(null, null), response("answer", 2500));

        AssertHelper.assertEquals(config, result.getScore(), 0.0, "Score at more than twice the limit");
    }
}
