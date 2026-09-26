package automation.core.ai;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestBase;
import automation.core.TestVariables;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class LabelSheetTest extends TestBase
{
    private EvalRecord evalRecord(String caseId, boolean passed, String output)
    {
        return new EvalRecord(
            EvalCase.builder().caseId(caseId).input("question " + caseId).build(),
            EvalResponse.builder().caseId(caseId).output(output).success(true).build(),
            AggregatedEvaluation.builder()
                .caseId(caseId)
                .overallScore(passed ? 1.0 : 0.0)
                .finalPass(passed)
                .topIssues(passed ? List.of() : List.of("Required phrase MISSING: 'refund'"))
                .build());
    }

    // Stands in for a person filling in the humanLabel column of the spreadsheet
    private void fillHumanLabels(File sheet, String... humanLabels) throws IOException, CsvException
    {
        List<String[]> rows;
        try (CSVReader reader = new CSVReader(new FileReader(sheet)))
        {
            rows = reader.readAll();
        }
        for (int i = 0; i < humanLabels.length; i++)
        {
            rows.get(i + 1)[6] = humanLabels[i];
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(sheet)))
        {
            writer.writeAll(rows);
        }
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void labelledRowsReadBack_blankLabelsSkipped(Config config) throws IOException, CsvException
    {
        File sheet = new File(Files.createTempDirectory("labels").toFile(), "labels.csv");
        LabelSheet.write(sheet, List.of(
            evalRecord("CASE-001", true, "An answer, with a comma\nand a second line"),
            evalRecord("CASE-002", false, "Another answer"),
            evalRecord("CASE-003", true, "An answer nobody labelled")));
        fillHumanLabels(sheet, "fail", "FAIL", "");

        List<LabelSheet.LabelledCase> labelled = LabelSheet.read(sheet);

        AssertHelper.assertEquals(config, labelled.size(), 2, "Labelled rows read back, blank label skipped");
        AssertHelper.assertEquals(config, labelled.get(0).caseId(), "CASE-001", "First labelled case survives commas and new lines");
        AssertHelper.assertFalse(config, labelled.get(0).humanPass(), "You failed CASE-001");
        AssertHelper.assertTrue(config, labelled.get(0).evalPass(), "The evals passed CASE-001");
        AssertHelper.assertFalse(config, labelled.get(1).humanPass(), "Labels are read regardless of case");
    }
}
