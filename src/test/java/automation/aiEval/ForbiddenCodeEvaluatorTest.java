package automation.aiEval;

import org.testng.Assert;
import org.testng.annotations.Test;

import automation.aiEval.evaluator.ForbiddenCodeEvaluator;
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

public class ForbiddenCodeEvaluatorTest extends TestBase
{
    private final ForbiddenCodeEvaluator evaluator = new ForbiddenCodeEvaluator();

    private AITestCase buildTestCase(List<String> forbiddenCodes, String severity)
    {
        ExpectedOutcome expected = ExpectedOutcome.builder()
            .forbiddenCodes(forbiddenCodes != null ? forbiddenCodes : new ArrayList<>())
            .build();
        return AITestCase.builder()
            .caseId("TEST-001")
            .severity(severity)
            .input(InputPayload.builder().chartText("test").build())
            .expected(expected)
            .build();
    }

    private AIExecutionResult buildResult(List<String> predictedCodes)
    {
        return AIExecutionResult.builder()
            .caseId("TEST-001")
            .predictedCodes(predictedCodes)
            .success(true)
            .build();
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void noForbiddenCodesDefined_passes(Config config)
    {
        AITestCase tc = buildTestCase(null, "normal");
        AIExecutionResult result = buildResult(List.of("E86.0"));

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 1.0, 0.001);
        AssertHelper.assertTrue(config, er.isPassed(), "Evaluation should pass when no forbidden codes defined");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void forbiddenCodeDetected_fails(Config config)
    {
        AITestCase tc = buildTestCase(List.of("G43.909"), "normal");
        AIExecutionResult result = buildResult(List.of("G43.909", "E86.0"));

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 0.0, 0.001);
        AssertHelper.assertFalse(config, er.isPassed(), "Evaluation should fail when forbidden code detected");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void noViolations_passes(Config config)
    {
        AITestCase tc = buildTestCase(List.of("G43.909"), "normal");
        AIExecutionResult result = buildResult(List.of("E86.0"));

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 1.0, 0.001);
        AssertHelper.assertTrue(config, er.isPassed(), "Evaluation should pass when no forbidden codes violated");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void criticalSeverityViolation_hasExtraFinding(Config config)
    {
        AITestCase tc = buildTestCase(List.of("G43.909"), "critical");
        AIExecutionResult result = buildResult(List.of("G43.909"));

        EvaluationResult er = evaluator.evaluate(tc, result);

        AssertHelper.assertFalse(config, er.isPassed(), "Evaluation should fail on forbidden code in critical case");
        AssertHelper.assertNotNull(config, er.getFindings(), "Findings list should not be null");

        boolean hasExtraFinding = er.getFindings().stream()
            .anyMatch(f -> f.contains("CRITICAL severity case with forbidden code violation"));
        AssertHelper.assertTrue(config, hasExtraFinding, "Should have extra finding for CRITICAL severity violation");
    }
}
