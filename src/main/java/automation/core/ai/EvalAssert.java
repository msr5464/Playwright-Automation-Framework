package automation.core.ai;

import automation.core.Config;
import automation.core.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * The eval counterpart of AssertHelper: scores a response and softly fails the test when the case fails.
 * Every call is also recorded for the run report {@link EvalRunListener} writes after the suite.
 */
public class EvalAssert
{
    public static void meets(Config config, EvalCase evalCase, EvalResponse response, List<Evaluator> evaluators)
    {
        List<EvaluationResult> results = new ArrayList<>();
        for (Evaluator evaluator : evaluators)
        {
            EvaluationResult result = evaluator.evaluate(evalCase, response);
            results.add(result);
            if (result.isPassed())
            {
                Log.comment(config, "[" + result.getEvaluatorName() + "] " + result.getSummary());
            }
            else
            {
                Log.warning(config, "[" + result.getEvaluatorName() + "] " + result.getSummary());
            }
        }

        AggregatedEvaluation evaluation = new ScoreAggregator(AggregationConfig.fromConfig(config))
            .aggregate(evalCase, response, results);
        EvalRunListener.record(config.testcaseClass, new EvalRecord(evalCase, response, evaluation));

        String verdict = "Case " + evalCase.getCaseId() + " scored " + String.format("%.2f", evaluation.getOverallScore());
        if (evaluation.isFinalPass())
        {
            Log.pass(config, "✔ PASS: " + verdict);
        }
        else
        {
            Log.fail(config, "✘ FAIL: " + verdict + " | " + String.join("; ", evaluation.getTopIssues()));
        }
    }
}
