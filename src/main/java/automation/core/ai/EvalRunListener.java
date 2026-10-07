package automation.core.ai;

import automation.core.Config;
import automation.core.Log;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * After the suite, writes one run per eval test class to {@code {resultsDirectory}/ai-eval/{TestClass}/runs/{runId}/}:
 * responses, evaluations, summary, a labelling sheet and an HTML report compared with that class's previous run.
 * The pass-rate gate is reported, not enforced — a failing case already fails its own test.
 */
public class EvalRunListener implements ISuiteListener
{
    private static final Map<String, Queue<EvalRecord>> RECORDS = new ConcurrentHashMap<>();

    static void record(String testClass, EvalRecord evalRecord)
    {
        RECORDS.computeIfAbsent(testClass, key -> new ConcurrentLinkedQueue<>()).add(evalRecord);
    }

    @Override
    public void onFinish(ISuite suite)
    {
        if (RECORDS.isEmpty())
        {
            return;
        }
        Config config = new Config();
        RunContext context = RunContext.builder()
            .environment(Config.environment)
            .buildVersion(config.getRunTimeProperty("ai.eval.buildVersion", "unknown"))
            .modelVersion(config.getRunTimeProperty("ai.eval.modelVersion", "unknown"))
            .promptVersion(config.getRunTimeProperty("ai.eval.promptVersion", "unknown"))
            .build();
        double minPassRate = Double.parseDouble(config.getRunTimeProperty("ai.eval.minPassRate", "0.80"));
        String runId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));

        for (Map.Entry<String, Queue<EvalRecord>> entry : RECORDS.entrySet())
        {
            String testClass = entry.getKey().substring(entry.getKey().lastIndexOf('.') + 1);
            File classDirectory = new File(Config.resultsDirectory, "ai-eval" + File.separator + testClass);
            try
            {
                writeRun(classDirectory, runId, new ArrayList<>(entry.getValue()), context, minPassRate);
            }
            catch (IOException e)
            {
                Log.error("AI eval report for " + testClass + " was not written: " + e.getMessage());
            }
        }
        RECORDS.clear();
    }

    static File writeRun(File classDirectory, String runId, List<EvalRecord> records, RunContext context, double minPassRate)
        throws IOException
    {
        ResultStore store = new ResultStore(classDirectory.getPath());
        List<EvalCase> evalCases = new ArrayList<>();
        List<AggregatedEvaluation> evals = new ArrayList<>();
        for (EvalRecord evalRecord : records)
        {
            evalCases.add(evalRecord.evalCase());
            evals.add(evalRecord.evaluation());
            store.storeResponse(runId, evalRecord.response());
            store.storeEvaluation(runId, evalRecord.evaluation());
        }

        RunSummary summary = new SummaryBuilder().build(runId, context, evals, evalCases);
        store.storeSummary(summary, evals);

        File runDirectory = new File(classDirectory, "runs" + File.separator + runId);
        LabelSheet.write(new File(runDirectory, "labels.csv"), records);
        BaselineComparison baseline = compareWithPreviousRun(store, classDirectory, summary, evals);
        File report = new File(runDirectory, "report.html");
        new HtmlReportGenerator().generate(summary, evals, evalCases, baseline, report);

        String gate;
        try
        {
            new CIGate(minPassRate).evaluate(summary);
            gate = "gate PASSED (min pass rate " + minPassRate + ")";
        }
        catch (CIGateException e)
        {
            gate = e.getMessage().replace("\n", " ");
        }
        Log.info(String.format("AI eval %s: %d/%d cases passed, %s. Report: %s",
            classDirectory.getName(), summary.getPassedCases(), summary.getTotalCases(), gate, report));
        return runDirectory;
    }

    // The latest earlier run of the same test class, so each run is compared with the one before it
    private static BaselineComparison compareWithPreviousRun(
        ResultStore store,
        File classDirectory,
        RunSummary summary,
        List<AggregatedEvaluation> evals) throws IOException
    {
        File[] earlierRuns = new File(classDirectory, "runs").listFiles(run -> run.isDirectory()
            && run.getName().compareTo(summary.getRunId()) < 0
            && new File(run, "summary.json").exists());
        if (earlierRuns == null || earlierRuns.length == 0)
        {
            return null;
        }
        String previousRunId = Arrays.stream(earlierRuns).map(File::getName).max(Comparator.naturalOrder()).get();
        return new BaselineComparator()
            .compare(store.loadSummary(previousRunId), summary, store.loadEvaluations(previousRunId), evals);
    }
}
