package automation.core.ai;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestBase;
import automation.core.TestVariables;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class EvalRunListenerTest extends TestBase
{
    private EvalRecord evalRecord(String caseId, boolean passed)
    {
        return new EvalRecord(
            EvalCase.builder().caseId(caseId).suite("listener").input("question " + caseId).build(),
            EvalResponse.builder().caseId(caseId).output("answer " + caseId).success(true).build(),
            AggregatedEvaluation.builder()
                .caseId(caseId)
                .overallScore(passed ? 1.0 : 0.0)
                .finalPass(passed)
                .riskStatus("standard")
                .evaluatorResults(List.of())
                .dimensionScores(Map.of())
                .topIssues(passed ? List.of() : List.of("Required phrase MISSING: 'refund'"))
                .build());
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void writeRun_writesRunFilesAndComparesWithPreviousRun(Config config) throws IOException
    {
        File classDirectory = Files.createTempDirectory("ai-eval").toFile();
        List<EvalRecord> records = List.of(evalRecord("CASE-001", true), evalRecord("CASE-002", false));
        RunContext context = RunContext.builder().environment("test").build();

        File firstRun = EvalRunListener.writeRun(classDirectory, "20260101-000000-000", records, context, 0.8);
        File secondRun = EvalRunListener.writeRun(classDirectory, "20260101-000001-000", records, context, 0.8);

        AssertHelper.assertTrue(config, new File(firstRun, "summary.json").exists(), "Run writes summary.json");
        AssertHelper.assertTrue(config, new File(firstRun, "labels.csv").exists(), "Run writes a labelling sheet");
        AssertHelper.assertFalse(config, Files.readString(new File(firstRun, "report.html").toPath()).contains("Baseline Comparison"),
            "First run has no earlier run to compare with");
        AssertHelper.assertContains(config, Files.readString(new File(secondRun, "report.html").toPath()), "Baseline Comparison",
            "Second run is compared with the first");
    }
}
