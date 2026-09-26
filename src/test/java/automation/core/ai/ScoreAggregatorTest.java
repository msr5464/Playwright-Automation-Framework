package automation.core.ai;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestBase;
import automation.core.TestVariables;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class ScoreAggregatorTest extends TestBase
{
    private final EvalCase standardCase = EvalCase.builder().caseId("CASE-001").input("question").build();
    private final EvalCase highRiskCase = EvalCase.builder().caseId("CASE-002").input("question").riskLevel("high").build();
    private final EvalResponse successfulResponse = EvalResponse.builder().caseId("CASE-001").output("answer").success(true).build();

    private EvaluationResult result(String evaluatorName, double score)
    {
        boolean passed = score >= 1.0;
        return EvaluationResult.builder()
            .evaluatorName(evaluatorName)
            .score(score)
            .passed(passed)
            .findings(passed ? List.of() : List.of("Failure in " + evaluatorName))
            .build();
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void allEvaluatorsPass_casePasses(Config config)
    {
        AggregatedEvaluation evaluation = new ScoreAggregator(AggregationConfig.builder().build())
            .aggregate(standardCase, successfulResponse, List.of(result("mustContain", 1.0), result("latency", 1.0)));

        AssertHelper.assertEquals(config, evaluation.getOverallScore(), 1.0, "Overall score");
        AssertHelper.assertTrue(config, evaluation.isFinalPass(), "Case passes when every evaluator passes");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void mustNotContainFails_hardFailDespitePassingScore(Config config)
    {
        AggregationConfig lowThreshold = AggregationConfig.builder().defaultMinScore(0.5).build();
        AggregatedEvaluation evaluation = new ScoreAggregator(lowThreshold).aggregate(standardCase, successfulResponse,
            List.of(result("mustContain", 1.0), result("latency", 1.0), result("mustNotContain", 0.0)));

        AssertHelper.assertTrue(config, evaluation.getOverallScore() >= 0.5, "Score clears the 0.5 threshold");
        AssertHelper.assertFalse(config, evaluation.isFinalPass(), "A failed mustNotContain check fails the case anyway");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void newEvaluatorFailing_lowersScoreAndFailsCase(Config config)
    {
        // The old aggregator ignored evaluators it didn't know by name, so an LLM judge could fail while the case passed
        AggregatedEvaluation evaluation = new ScoreAggregator(AggregationConfig.builder().build())
            .aggregate(standardCase, successfulResponse, List.of(result("mustContain", 1.0), result("llmJudge", 0.0)));

        AssertHelper.assertEquals(config, evaluation.getOverallScore(), 0.5, "Overall score with a failing llmJudge");
        AssertHelper.assertFalse(config, evaluation.isFinalPass(), "A failing new evaluator fails the case");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void failedCall_failsCase(Config config)
    {
        EvalResponse failedResponse = EvalResponse.builder().caseId("CASE-001").success(false).errorMessage("HTTP 500").build();
        AggregatedEvaluation evaluation = new ScoreAggregator(AggregationConfig.builder().build())
            .aggregate(standardCase, failedResponse, List.of(result("mustContain", 1.0), result("mustNotContain", 1.0)));

        AssertHelper.assertFalse(config, evaluation.isFinalPass(), "A failed call fails the case even when evaluators pass");
        AssertHelper.assertTrue(config, evaluation.getTopIssues().contains("Call failed: HTTP 500"), "Top issues name the failed call");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void noEvaluators_failsCase(Config config)
    {
        AggregatedEvaluation evaluation = new ScoreAggregator(AggregationConfig.builder().build())
            .aggregate(standardCase, successfulResponse, List.of());

        AssertHelper.assertFalse(config, evaluation.isFinalPass(), "A case no evaluator checked cannot pass");
        AssertHelper.assertTrue(config, evaluation.getTopIssues().contains("No evaluators ran"), "Top issues say no evaluators ran");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void configuredWeights_changeOverallScore(Config config)
    {
        AggregationConfig weighted = AggregationConfig.builder().weights(Map.of("mustContain", 9.0)).build();
        AggregatedEvaluation evaluation = new ScoreAggregator(weighted)
            .aggregate(standardCase, successfulResponse, List.of(result("mustContain", 1.0), result("latency", 0.0)));

        AssertHelper.assertEquals(config, evaluation.getOverallScore(), 0.9, "Weighted score (mustContain 9 : latency 1)");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void highRiskCase_needsHigherScore(Config config)
    {
        ScoreAggregator aggregator = new ScoreAggregator(AggregationConfig.builder().weights(Map.of("mustContain", 9.0)).build());
        List<EvaluationResult> results = List.of(result("mustContain", 1.0), result("latency", 0.0));

        AssertHelper.assertTrue(config, aggregator.aggregate(standardCase, successfulResponse, results).isFinalPass(),
            "Score 0.9 passes a standard case (threshold 0.85)");
        AssertHelper.assertFalse(config, aggregator.aggregate(highRiskCase, successfulResponse, results).isFinalPass(),
            "Score 0.9 fails a high-risk case (threshold 0.95)");
    }
}
