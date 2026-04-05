package automation.aiEval;

import org.testng.Assert;
import org.testng.annotations.Test;

import automation.aiEval.loader.AITestCaseLoader;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.InputPayload;
import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestBase;
import automation.core.TestVariables;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AITestCaseLoaderTest extends TestBase
{
    private final AITestCaseLoader loader = new AITestCaseLoader();

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void loadFromFile_validJson_returnsTestCase(Config config) throws Exception
    {
        File resourceFile = new File("src/main/resources/ai-testcases/smoke/MC-00124.json");
        List<AITestCase> cases = loader.loadFromFile(resourceFile);

        AssertHelper.assertFalse(config, cases.isEmpty(), "Should have loaded at least one test case");
        AITestCase tc = cases.get(0);
        AssertHelper.assertEquals(config, tc.getCaseId(), "MC-00124", "Case ID should match");
        AssertHelper.assertEquals(config, tc.getRiskLevel(), "high", "Risk level should be high");
        AssertHelper.assertNotNull(config, tc.getExpected(), "Expected outcome should not be null");
        AssertHelper.assertTrue(config,
            tc.getExpected().getExpectedCodes().contains("E86.0"),
            "Expected codes should contain E86.0");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void loadFromFile_missingCaseId_throwsException(Config config) throws Exception
    {
        File tempFile = Files.createTempFile("test-case-no-id", ".json").toFile();
        tempFile.deleteOnExit();

        try (FileWriter fw = new FileWriter(tempFile))
        {
            fw.write("{\"suite\": \"smoke\", \"input\": {\"chartText\": \"test\"}}");
        }

        try
        {
            loader.loadFromFile(tempFile);
            Assert.fail("Expected IllegalArgumentException for missing caseId");
        }
        catch (IllegalArgumentException e)
        {
            AssertHelper.assertTrue(config,
                e.getMessage().contains("missing required field: caseId"),
                "Exception message should mention missing caseId");
        }
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void filterByTag_returnsOnlyMatchingCases(Config config)
    {
        AITestCase case1 = AITestCase.builder()
            .caseId("TC-001")
            .tags(List.of("smoke", "critical"))
            .input(InputPayload.builder().chartText("test").build())
            .build();

        AITestCase case2 = AITestCase.builder()
            .caseId("TC-002")
            .tags(List.of("regression"))
            .input(InputPayload.builder().chartText("test2").build())
            .build();

        List<AITestCase> all = new ArrayList<>();
        all.add(case1);
        all.add(case2);

        List<AITestCase> filtered = loader.filterByTag(all, "smoke");

        AssertHelper.assertEquals(config, filtered.size(), 1, "Only one case should match the 'smoke' tag");
        AssertHelper.assertEquals(config, filtered.get(0).getCaseId(), "TC-001", "Filtered case should be TC-001");
    }
}
