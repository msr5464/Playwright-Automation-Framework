package automation.aiEval.storage;

import automation.aiEval.model.AggregatedEvaluation;
import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.RunSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResultStore
{
    private final String outputDir;
    private final ObjectMapper mapper;

    public ResultStore(String outputDir)
    {
        this.outputDir = outputDir;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void storeExecutionResult(AIExecutionResult result) throws IOException
    {
        File dir = new File(outputDir + "/runs/" + result.getRunId() + "/raw-results");
        dir.mkdirs();
        File file = new File(dir, result.getCaseId() + ".json");
        mapper.writeValue(file, result);
    }

    public void storeEvaluationResult(AggregatedEvaluation eval) throws IOException
    {
        File dir = new File(outputDir + "/runs/" + eval.getRunId() + "/evaluations");
        dir.mkdirs();
        File file = new File(dir, eval.getCaseId() + "-eval.json");
        mapper.writeValue(file, eval);
    }

    public void storeSummary(RunSummary summary) throws IOException
    {
        storeSummary(summary, new ArrayList<>());
    }

    public void storeSummary(RunSummary summary, List<AggregatedEvaluation> evals) throws IOException
    {
        File dir = new File(outputDir + "/runs/" + summary.getRunId());
        dir.mkdirs();

        File jsonFile = new File(dir, "summary.json");
        mapper.writeValue(jsonFile, summary);

        File csvFile = new File(dir, "summary.csv");
        writeSummaryCsv(evals, csvFile);
    }

    public RunSummary loadSummary(String runId) throws IOException
    {
        File file = new File(outputDir + "/runs/" + runId + "/summary.json");
        return mapper.readValue(file, RunSummary.class);
    }

    public List<AggregatedEvaluation> loadEvaluations(String runId) throws IOException
    {
        File dir = new File(outputDir + "/runs/" + runId + "/evaluations");
        List<AggregatedEvaluation> results = new ArrayList<>();

        if (!dir.isDirectory())
        {
            return results;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith("-eval.json"));
        if (files == null)
        {
            return results;
        }

        for (File file : files)
        {
            results.add(mapper.readValue(file, AggregatedEvaluation.class));
        }

        return results;
    }

    private void writeSummaryCsv(List<AggregatedEvaluation> evals, File csvFile) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("caseId,overallScore,finalPass,riskStatus,topIssue\n");

        for (AggregatedEvaluation eval : evals)
        {
            String topIssue = (eval.getTopIssues() != null && !eval.getTopIssues().isEmpty())
                ? eval.getTopIssues().get(0) : "";
            sb.append(escapeCsv(eval.getCaseId())).append(",")
              .append(String.format("%.4f", eval.getOverallScore())).append(",")
              .append(eval.isFinalPass()).append(",")
              .append(escapeCsv(eval.getRiskStatus())).append(",")
              .append(escapeCsv(topIssue)).append("\n");
        }

        try (FileWriter writer = new FileWriter(csvFile))
        {
            writer.write(sb.toString());
        }
    }

    private String escapeCsv(String value)
    {
        if (value == null)
        {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
        {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
