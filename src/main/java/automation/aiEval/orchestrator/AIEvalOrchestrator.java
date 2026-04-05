package automation.aiEval.orchestrator;

import automation.aiEval.aggregator.ScoreAggregator;
import automation.aiEval.evaluator.AIEvaluator;
import automation.aiEval.model.AggregatedEvaluation;
import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.EvaluationResult;
import automation.aiEval.model.RunContext;
import automation.aiEval.model.RunSummary;
import automation.aiEval.report.HtmlReportGenerator;
import automation.aiEval.report.SummaryBuilder;
import automation.aiEval.runner.AIRunner;
import automation.aiEval.storage.ResultStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AIEvalOrchestrator
{
    private static final Logger LOG = Logger.getLogger(AIEvalOrchestrator.class.getName());

    private final AIRunner runner;
    private final List<AIEvaluator> evaluators;
    private final ScoreAggregator aggregator;
    private final ResultStore store;
    private final SummaryBuilder summaryBuilder;
    private final HtmlReportGenerator reportGenerator;
    private final CIGate ciGate;

    public AIEvalOrchestrator(
        AIRunner runner,
        List<AIEvaluator> evaluators,
        ScoreAggregator aggregator,
        ResultStore store,
        SummaryBuilder summaryBuilder,
        HtmlReportGenerator reportGenerator,
        CIGate ciGate)
    {
        this.runner = runner;
        this.evaluators = evaluators;
        this.aggregator = aggregator;
        this.store = store;
        this.summaryBuilder = summaryBuilder;
        this.reportGenerator = reportGenerator;
        this.ciGate = ciGate;
    }

    public RunSummary run(List<AITestCase> testCases, RunContext runContext, File outputDir)
    {
        List<AggregatedEvaluation> allEvals = new ArrayList<>();

        for (AITestCase testCase : testCases)
        {
            AIExecutionResult executionResult = runner.execute(testCase, runContext);

            try
            {
                store.storeExecutionResult(executionResult);
            }
            catch (IOException e)
            {
                LOG.warning("Failed to store execution result for " + testCase.getCaseId() + ": " + e.getMessage());
            }

            List<EvaluationResult> evalResults = new ArrayList<>();
            for (AIEvaluator evaluator : evaluators)
            {
                evalResults.add(evaluator.evaluate(testCase, executionResult));
            }

            AggregatedEvaluation agg = aggregator.aggregate(testCase, executionResult, evalResults);

            try
            {
                store.storeEvaluationResult(agg);
            }
            catch (IOException e)
            {
                LOG.warning("Failed to store evaluation result for " + testCase.getCaseId() + ": " + e.getMessage());
            }

            allEvals.add(agg);
        }

        RunSummary summary = summaryBuilder.build(runContext.getRunId(), runContext, allEvals, testCases);

        try
        {
            store.storeSummary(summary, allEvals);
        }
        catch (IOException e)
        {
            LOG.warning("Failed to store summary: " + e.getMessage());
        }

        try
        {
            reportGenerator.generate(summary, allEvals, testCases, null, new File(outputDir, "report.html"));
        }
        catch (IOException e)
        {
            LOG.warning("Failed to generate HTML report: " + e.getMessage());
        }

        ciGate.evaluate(summary);

        return summary;
    }
}
