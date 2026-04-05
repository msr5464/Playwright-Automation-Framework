package automation.aiEval;

import org.testng.Assert;
import org.testng.annotations.Test;

import automation.aiEval.evaluator.CodeMatchEvaluator;
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

public class CodeMatchEvaluatorTest extends TestBase
{
    private final CodeMatchEvaluator evaluator = new CodeMatchEvaluator();

    private AITestCase buildTestCase(List<String> expectedCodes, List<String> allowedAlts)
    {
        ExpectedOutcome expected = ExpectedOutcome.builder()
            .expectedCodes(expectedCodes)
            .allowedAlternativeCodes(allowedAlts != null ? allowedAlts : new ArrayList<>())
            .build();
        return AITestCase.builder()
            .caseId("TEST-001")
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
    public void allCodesMatch_scoreIsOne(Config config)
    {
        AITestCase tc = buildTestCase(List.of("E86.0", "I10"), null);
        AIExecutionResult result = buildResult(List.of("E86.0", "I10", "Z00.00"));

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 1.0, 0.001);
        AssertHelper.assertTrue(config, er.isPassed(), "All expected codes matched - evaluation should pass");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void partialMatch_scoreIsProportional(Config config)
    {
        AITestCase tc = buildTestCase(List.of("E86.0", "I10"), null);
        AIExecutionResult result = buildResult(List.of("E86.0"));

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 0.5, 0.001);
        AssertHelper.assertFalse(config, er.isPassed(), "Only partial code match - evaluation should fail");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void noMatch_scoreIsZero(Config config)
    {
        AITestCase tc = buildTestCase(List.of("E86.0", "I10"), null);
        AIExecutionResult result = buildResult(List.of("G43.909"));

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 0.0, 0.001);
        AssertHelper.assertFalse(config, er.isPassed(), "No expected codes matched - evaluation should fail");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void noExpectedCodes_scoreIsOne(Config config)
    {
        AITestCase tc = buildTestCase(null, null);
        AIExecutionResult result = buildResult(List.of("E86.0"));

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 1.0, 0.001);
        AssertHelper.assertTrue(config, er.isPassed(), "No expected codes defined - evaluation should pass");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void allowedAlternativeAccepted(Config config)
    {
        AITestCase tc = buildTestCase(List.of("E86.0"), List.of("E86.1"));
        AIExecutionResult result = buildResult(List.of("E86.1"));

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 1.0, 0.001);
        AssertHelper.assertTrue(config, er.isPassed(), "Allowed alternative code predicted - evaluation should pass");
    }
}
