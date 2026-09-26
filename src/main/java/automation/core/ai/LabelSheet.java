package automation.core.ai;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A spreadsheet for judging eval outputs by hand. Every run writes one ({@code labels.csv}) with empty
 * {@code humanLabel} and {@code critique} columns. Fill in {@code pass} or {@code fail} plus one line on why;
 * {@link #read(File)} brings the labels back so {@link Calibration} can compare them with the automated verdicts.
 */
public class LabelSheet
{
    private static final String[] HEADER =
        {"caseId", "input", "output", "evalVerdict", "evalScore", "topIssue", "humanLabel", "critique"};

    /** One case you labelled: your verdict next to the automated one. */
    public record LabelledCase(String caseId, boolean humanPass, boolean evalPass)
    {
    }

    public static void write(File file, List<EvalRecord> records) throws IOException
    {
        file.getParentFile().mkdirs();
        try (CSVWriter writer = new CSVWriter(new FileWriter(file)))
        {
            writer.writeNext(HEADER);
            for (EvalRecord evalRecord : records)
            {
                AggregatedEvaluation evaluation = evalRecord.evaluation();
                writer.writeNext(new String[]{
                    evalRecord.evalCase().getCaseId(),
                    evalRecord.evalCase().getInput(),
                    evalRecord.response().getOutput(),
                    evaluation.isFinalPass() ? "pass" : "fail",
                    String.format("%.2f", evaluation.getOverallScore()),
                    evaluation.getTopIssues().isEmpty() ? "" : evaluation.getTopIssues().get(0),
                    "",
                    ""
                });
            }
        }
    }

    /** The rows you labelled; rows whose humanLabel isn't "pass" or "fail" are skipped. */
    public static List<LabelledCase> read(File file) throws IOException
    {
        List<LabelledCase> labelled = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(file)))
        {
            List<String[]> rows = reader.readAll();
            List<String> header = Arrays.asList(rows.get(0));
            int caseIdColumn = header.indexOf("caseId");
            int evalVerdictColumn = header.indexOf("evalVerdict");
            int humanLabelColumn = header.indexOf("humanLabel");
            for (String[] row : rows.subList(1, rows.size()))
            {
                String humanLabel = row[humanLabelColumn].trim().toLowerCase();
                if (humanLabel.equals("pass") || humanLabel.equals("fail"))
                {
                    labelled.add(new LabelledCase(row[caseIdColumn], humanLabel.equals("pass"),
                        row[evalVerdictColumn].equals("pass")));
                }
            }
        }
        catch (CsvException e)
        {
            throw new IOException("Could not read label sheet " + file + ": " + e.getMessage(), e);
        }
        return labelled;
    }
}
