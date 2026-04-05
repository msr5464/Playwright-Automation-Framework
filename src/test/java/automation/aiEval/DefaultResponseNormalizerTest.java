package automation.aiEval;

import org.testng.annotations.Test;

import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.InputPayload;
import automation.aiEval.model.RunContext;
import automation.aiEval.normalizer.DefaultResponseNormalizer;
import automation.aiEval.runner.RawServiceResponse;
import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestBase;
import automation.core.TestVariables;

import java.util.HashMap;

public class DefaultResponseNormalizerTest extends TestBase
{
    private final DefaultResponseNormalizer normalizer = new DefaultResponseNormalizer();

    private AITestCase buildTestCase()
    {
        return AITestCase.builder()
            .caseId("MC-001")
            .input(InputPayload.builder().chartText("test").build())
            .build();
    }

    private RunContext buildRunContext()
    {
        return RunContext.builder()
            .runId("run-001")
            .environment("test")
            .buildVersion("1.0")
            .modelVersion("v1")
            .build();
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void completeResponse_extractsAllFields(Config config)
    {
        String body = "{"
            + "\"predictedCodes\":[\"E86.0\"],"
            + "\"rationale\":\"Patient shows dehydration signs\","
            + "\"auditTrail\":[{\"step\":\"review\",\"note\":\"evidence found\"}]"
            + "}";

        RawServiceResponse raw = RawServiceResponse.builder()
            .httpStatus(200)
            .body(body)
            .latencyMs(500)
            .headers(new HashMap<>())
            .build();

        AIExecutionResult result = normalizer.normalize(raw, buildTestCase(), buildRunContext());

        AssertHelper.assertTrue(config, result.isSuccess(), "Normalizer should mark 200 response as success");
        AssertHelper.assertEquals(config, result.getCaseId(), "MC-001", "Case ID should be preserved");
        AssertHelper.assertEquals(config, result.getRunId(), "run-001", "Run ID should be preserved");
        AssertHelper.assertNotNull(config, result.getPredictedCodes(), "Predicted codes should not be null");
        AssertHelper.assertTrue(config, result.getPredictedCodes().contains("E86.0"), "Predicted codes should contain E86.0");
        AssertHelper.assertEquals(config, result.getRationale(), "Patient shows dehydration signs", "Rationale should be extracted");
        AssertHelper.assertNotNull(config, result.getAuditTrail(), "Audit trail should not be null");
        AssertHelper.assertTrue(config, !result.getAuditTrail().isEmpty(), "Audit trail should not be empty");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void responseMissingOptionalFields_stillSucceeds(Config config)
    {
        String body = "{\"predictedCodes\":[\"I10\"]}";

        RawServiceResponse raw = RawServiceResponse.builder()
            .httpStatus(200)
            .body(body)
            .latencyMs(300)
            .headers(new HashMap<>())
            .build();

        AIExecutionResult result = normalizer.normalize(raw, buildTestCase(), buildRunContext());

        AssertHelper.assertTrue(config, result.isSuccess(), "Should succeed even with optional fields missing");
        AssertHelper.assertNotNull(config, result.getPredictedCodes(), "Predicted codes should not be null");
        AssertHelper.assertTrue(config, result.getPredictedCodes().contains("I10"), "Predicted codes should contain I10");
        AssertHelper.assertTrue(config, result.getRationale() == null, "Rationale should be null when absent");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void alternativeCodeKeys_extractedCorrectly(Config config)
    {
        // Response uses "codes" key instead of "predictedCodes"
        String body = "{\"codes\":[\"E86.0\",\"J18.9\"],\"reasoning\":\"clear signs\"}";

        RawServiceResponse raw = RawServiceResponse.builder()
            .httpStatus(200)
            .body(body)
            .latencyMs(200)
            .headers(new HashMap<>())
            .build();

        AIExecutionResult result = normalizer.normalize(raw, buildTestCase(), buildRunContext());

        AssertHelper.assertTrue(config, result.getPredictedCodes().contains("E86.0"), "Should extract codes from 'codes' key");
        AssertHelper.assertEquals(config, result.getRationale(), "clear signs", "Should extract rationale from 'reasoning' key");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void malformedBody_doesNotThrow(Config config)
    {
        RawServiceResponse raw = RawServiceResponse.builder()
            .httpStatus(200)
            .body("NOT_VALID_JSON")
            .latencyMs(100)
            .headers(new HashMap<>())
            .build();

        AIExecutionResult result = normalizer.normalize(raw, buildTestCase(), buildRunContext());

        AssertHelper.assertNotNull(config, result, "Result should not be null for malformed body");
        AssertHelper.assertNotNull(config, result.getPredictedCodes(), "Predicted codes list should not be null");
        AssertHelper.assertTrue(config, result.getPredictedCodes().isEmpty(), "Predicted codes should be empty for malformed body");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void nonSuccessStatus_marksResultFailed(Config config)
    {
        RawServiceResponse raw = RawServiceResponse.builder()
            .httpStatus(500)
            .body("{\"error\":\"internal\"}")
            .latencyMs(100)
            .headers(new HashMap<>())
            .build();

        AIExecutionResult result = normalizer.normalize(raw, buildTestCase(), buildRunContext());

        AssertHelper.assertFalse(config, result.isSuccess(), "HTTP 500 response should be marked as failed");
    }
}
