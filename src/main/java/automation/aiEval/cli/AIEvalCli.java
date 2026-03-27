package automation.aiEval.cli;

import automation.aiEval.aggregator.AggregationConfig;
import automation.aiEval.aggregator.ScoreAggregator;
import automation.aiEval.comparator.BaselineComparator;
import automation.aiEval.evaluator.AIEvaluator;
import automation.aiEval.evaluator.AuditTrailEvaluator;
import automation.aiEval.evaluator.CodeMatchEvaluator;
import automation.aiEval.evaluator.ForbiddenCodeEvaluator;
import automation.aiEval.evaluator.LatencyEvaluator;
import automation.aiEval.evaluator.SchemaValidatorEvaluator;
import automation.aiEval.loader.AITestCaseLoader;
import automation.aiEval.model.AggregatedEvaluation;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.BaselineComparison;
import automation.aiEval.model.RunContext;
import automation.aiEval.model.RunSummary;
import automation.aiEval.orchestrator.AIEvalOrchestrator;
import automation.aiEval.orchestrator.CIGate;
import automation.aiEval.orchestrator.CIGateException;
import automation.aiEval.report.HtmlReportGenerator;
import automation.aiEval.report.SummaryBuilder;
import automation.aiEval.runner.ApiRunner;
import automation.aiEval.storage.ResultStore;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AIEvalCli
{
    public static void main(String[] args)
    {
        String suite = System.getProperty("aiEval.suite");
        String tag = System.getProperty("aiEval.tag");
        String environment = System.getProperty("aiEval.environment", "local");
        String baseUrl = System.getProperty("aiEval.baseUrl");
        String authToken = System.getProperty("aiEval.authToken");
        String outputDir = System.getProperty("aiEval.outputDir", "artifacts/ai-eval");
        String buildVersion = System.getProperty("aiEval.buildVersion", "unknown");
        String modelVersion = System.getProperty("aiEval.modelVersion", "unknown");
        String promptVersion = System.getProperty("aiEval.promptVersion", "unknown");
        String baselineRunId = System.getProperty("aiEval.baselineRunId");

        if (suite == null || baseUrl == null)
        {
            System.err.println("Required: -DaiEval.suite=<suite> -DaiEval.baseUrl=<url>");
            System.exit(1);
        }

        try
        {
            // Load test cases
            AITestCaseLoader loader = new AITestCaseLoader();
            File testCasesDir = new File("src/main/resources/ai-testcases");
            List<AITestCase> testCases = loader.loadSuite(testCasesDir, suite);

            if (tag != null && !tag.isBlank())
            {
                testCases = loader.filterByTag(testCases, tag);
            }

            if (testCases.isEmpty())
            {
                System.err.println("No test cases found for suite: " + suite
                    + (tag != null ? ", tag: " + tag : ""));
                System.exit(1);
            }

            // Build RunContext
            String runId = UUID.randomUUID().toString().substring(0, 8)
                + "-" + Instant.now().toEpochMilli();

            RunContext runContext = RunContext.builder()
                .runId(runId)
                .environment(environment)
                .baseUrl(baseUrl)
                .authToken(authToken)
                .buildVersion(buildVersion)
                .modelVersion(modelVersion)
                .promptVersion(promptVersion)
                .build();

            // Wire up components
            AggregationConfig aggConfig = AggregationConfig.builder().build();

            List<AIEvaluator> evaluators = new ArrayList<>();
            evaluators.add(new SchemaValidatorEvaluator());
            evaluators.add(new CodeMatchEvaluator());
            evaluators.add(new ForbiddenCodeEvaluator());
            evaluators.add(new LatencyEvaluator(8000));
            evaluators.add(new AuditTrailEvaluator());

            AIEvalOrchestrator orchestrator = new AIEvalOrchestrator(
                new ApiRunner(),
                evaluators,
                new ScoreAggregator(aggConfig),
                new ResultStore(outputDir),
                new SummaryBuilder(),
                new HtmlReportGenerator(),
                new CIGate(0.80)
            );

            File outputDirFile = new File(outputDir);
            ResultStore store = new ResultStore(outputDir);
            RunSummary summary = orchestrator.run(testCases, runContext, outputDirFile);

            // Optional baseline comparison
            if (baselineRunId != null && !baselineRunId.isBlank())
            {
                try
                {
                    RunSummary baselineSummary = store.loadSummary(baselineRunId);
                    List<AggregatedEvaluation> baselineEvals = store.loadEvaluations(baselineRunId);
                    List<AggregatedEvaluation> currentEvals = store.loadEvaluations(summary.getRunId());
                    BaselineComparison comparison = new BaselineComparator()
                        .compare(baselineSummary, summary, baselineEvals, currentEvals);
                    System.out.println("Baseline comparison: " + comparison.getNotableChanges());
                }
                catch (Exception e)
                {
                    System.err.println("Warning: baseline comparison failed: " + e.getMessage());
                }
            }

            System.out.println("AI Eval complete. Run ID: " + summary.getRunId());
            System.out.println("Pass rate: " + String.format("%.1f%%", summary.getPassRate() * 100)
                + " (" + summary.getPassedCases() + "/" + summary.getTotalCases() + ")");
        }
        catch (CIGateException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (Exception e)
        {
            System.err.println("AI Eval failed with error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
