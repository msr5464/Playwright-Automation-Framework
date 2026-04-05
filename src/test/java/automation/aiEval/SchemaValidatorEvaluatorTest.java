package automation.aiEval;

import org.testng.Assert;
import org.testng.annotations.Test;

import automation.aiEval.evaluator.SchemaValidatorEvaluator;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaValidatorEvaluatorTest extends TestBase
{
    private final SchemaValidatorEvaluator evaluator = new SchemaValidatorEvaluator();

    private AITestCase buildTestCase(List<String> rationaleMustContain, boolean requireAuditTrail)
    {
        Map<String, Object> customAssertions = new HashMap<>();
        customAssertions.put("requireAuditTrail", requireAuditTrail);

        ExpectedOutcome expected = ExpectedOutcome.builder()
            .rationaleMustContain(rationaleMustContain != null ? rationaleMustContain : new ArrayList<>())
            .customAssertions(customAssertions)
            .build();

        return AITestCase.builder()
            .caseId("TEST-001")
            .input(InputPayload.builder().chartText("test").build())
            .expected(expected)
            .build();
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void successFalse_fails(Config config)
    {
        AITestCase tc = buildTestCase(null, false);
        AIExecutionResult result = AIExecutionResult.builder()
            .caseId("TEST-001")
            .success(false)
            .predictedCodes(List.of("E86.0"))
            .rationale("some rationale")
            .build();

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 0.0, 0.001);
        AssertHelper.assertFalse(config, er.isPassed(), "Schema should fail when execution success=false");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void emptyPredictedCodes_fails(Config config)
    {
        AITestCase tc = buildTestCase(null, false);
        AIExecutionResult result = AIExecutionResult.builder()
            .caseId("TEST-001")
            .success(true)
            .predictedCodes(new ArrayList<>())
            .rationale("some rationale")
            .build();

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 0.0, 0.001);
        AssertHelper.assertFalse(config, er.isPassed(), "Schema should fail when no predicted codes returned");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void rationaleRequiredButMissing_fails(Config config)
    {
        AITestCase tc = buildTestCase(List.of("dehydration"), false);
        AIExecutionResult result = AIExecutionResult.builder()
            .caseId("TEST-001")
            .success(true)
            .predictedCodes(List.of("E86.0"))
            .rationale(null)
            .build();

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 0.0, 0.001);
        AssertHelper.assertFalse(config, er.isPassed(), "Schema should fail when rationale is required but missing");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void allPresent_passes(Config config)
    {
        AITestCase tc = buildTestCase(List.of("dehydration"), true);
        AIExecutionResult result = AIExecutionResult.builder()
            .caseId("TEST-001")
            .success(true)
            .predictedCodes(List.of("E86.0"))
            .rationale("Patient shows dehydration indicators")
            .auditTrail(List.of(Map.of("step", "code-selection")))
            .build();

        EvaluationResult er = evaluator.evaluate(tc, result);

        Assert.assertEquals(er.getScore(), 1.0, 0.001);
        AssertHelper.assertTrue(config, er.isPassed(), "Schema should pass when all required fields are present");
    }
}
