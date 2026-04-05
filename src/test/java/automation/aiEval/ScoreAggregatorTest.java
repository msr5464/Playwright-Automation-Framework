package automation.aiEval;

import org.testng.Assert;
import org.testng.annotations.Test;

import automation.aiEval.aggregator.AggregationConfig;
import automation.aiEval.aggregator.ScoreAggregator;
import automation.aiEval.model.AggregatedEvaluation;
import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.EvaluationResult;
import automation.aiEval.model.ExpectedOutcome;
import automation.aiEval.model.InputPayload;
import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestBase;
import automation.core.TestVariables;

import java.util.ArrayList;
import java.util.List;

public class ScoreAggregatorTest extends TestBase
{
    private AITestCase buildTestCase(String riskLevel)
    {
        return AITestCase.builder()
            .caseId("TEST-001")
            .riskLevel(riskLevel)
            .severity("normal")
            .input(InputPayload.builder().chartText("test").build())
            .expected(ExpectedOutcome.builder().build())
            .build();
    }

    private AIExecutionResult buildResult()
    {
        return AIExecutionResult.builder()
            .caseId("TEST-001")
            .runId("run-001")
            .success(true)
            .predictedCodes(List.of("E86.0"))
            .rationale("test rationale")
            .build();
    }

    private EvaluationResult passing(String name)
    {
        return EvaluationResult.builder()
            .evaluatorName(name)
            .score(1.0)
            .passed(true)
            .findings(new ArrayList<>())
            .build();
    }

    private EvaluationResult failing(String name, double score)
    {
        return EvaluationResult.builder()
            .evaluatorName(name)
            .score(score)
            .passed(false)
            .findings(List.of("Failure in " + name))
            .build();
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void allEvaluatorsPass_overallPassTrue(Config config)
    {
        AggregationConfig aggConfig = AggregationConfig.builder().build();
        ScoreAggregator aggregator = new ScoreAggregator(aggConfig);

        AITestCase tc = buildTestCase("low");
        AIExecutionResult result = buildResult();

        List<EvaluationResult> evals = new ArrayList<>();
        evals.add(passing("schema"));
        evals.add(passing("codeMatch"));
        evals.add(passing("forbiddenCode"));
        evals.add(passing("latency"));
        evals.add(passing("auditTrail"));

        AggregatedEvaluation agg = aggregator.aggregate(tc, result, evals);

        Assert.assertEquals(agg.getOverallScore(), 1.0, 0.001);
        AssertHelper.assertTrue(config, agg.isFinalPass(), "All evaluators passed - overall should pass");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void forbiddenCodeFails_hardFailEvenWithHighScore(Config config)
    {
        AggregationConfig aggConfig = AggregationConfig.builder()
            .failOnForbiddenCode(true)
            .build();
        ScoreAggregator aggregator = new ScoreAggregator(aggConfig);

        AITestCase tc = buildTestCase("low");
        AIExecutionResult result = buildResult();

        List<EvaluationResult> evals = new ArrayList<>();
        evals.add(passing("schema"));
        evals.add(passing("codeMatch"));
        evals.add(failing("forbiddenCode", 0.0));
        evals.add(passing("latency"));
        evals.add(passing("auditTrail"));

        AggregatedEvaluation agg = aggregator.aggregate(tc, result, evals);

        AssertHelper.assertFalse(config, agg.isFinalPass(),
            "Should hard-fail when forbidden code evaluator fails and failOnForbiddenCode=true");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void schemaFails_hardFailEvenWithHighScore(Config config)
    {
        AggregationConfig aggConfig = AggregationConfig.builder()
            .failOnSchemaError(true)
            .build();
        ScoreAggregator aggregator = new ScoreAggregator(aggConfig);

        AITestCase tc = buildTestCase("low");
        AIExecutionResult result = buildResult();

        List<EvaluationResult> evals = new ArrayList<>();
        evals.add(failing("schema", 0.0));
        evals.add(passing("codeMatch"));
        evals.add(passing("forbiddenCode"));
        evals.add(passing("latency"));
        evals.add(passing("auditTrail"));

        AggregatedEvaluation agg = aggregator.aggregate(tc, result, evals);

        AssertHelper.assertFalse(config, agg.isFinalPass(),
            "Should hard-fail when schema evaluator fails and failOnSchemaError=true");
    }
}
