package automation.core.ai;

/**
 * Scores one aspect of a response from 0.0 to 1.0.
 * {@link #getName()} is the key used for weights and hard-fail rules in {@link AggregationConfig}.
 */
public interface Evaluator
{
    EvaluationResult evaluate(EvalCase evalCase, EvalResponse response);

    String getName();
}
