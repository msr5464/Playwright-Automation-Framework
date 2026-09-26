package automation.core.ai;

/** One evaluated case of a run: what was asked, what came back, and the verdict. */
public record EvalRecord(EvalCase evalCase, EvalResponse response, AggregatedEvaluation evaluation)
{
}
