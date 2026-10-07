package automation.core.ai;

import java.util.ArrayList;
import java.util.List;

public class LatencyEvaluator implements Evaluator
{
    private final long maxLatencyMs;

    public LatencyEvaluator(long maxLatencyMs)
    {
        this.maxLatencyMs = maxLatencyMs;
    }

    @Override
    public String getName()
    {
        return "latency";
    }

    @Override
    public EvaluationResult evaluate(EvalCase evalCase, EvalResponse response)
    {
        List<String> findings = new ArrayList<>();
        List<String> evidence = new ArrayList<>();
        long latencyMs = response.getLatencyMs();
        double score;

        if (latencyMs <= maxLatencyMs)
        {
            score = 1.0;
            evidence.add("Latency " + latencyMs + "ms within threshold (" + maxLatencyMs + "ms)");
        }
        else if (latencyMs >= maxLatencyMs * 2)
        {
            score = 0.0;
            findings.add("Latency " + latencyMs + "ms exceeds double threshold (" + (maxLatencyMs * 2) + "ms)");
        }
        else
        {
            score = 1.0 - ((double) (latencyMs - maxLatencyMs) / (double) maxLatencyMs);
            findings.add("Latency " + latencyMs + "ms exceeds threshold (" + maxLatencyMs + "ms)");
        }

        return EvaluationResult.builder()
            .evaluatorName(getName())
            .caseId(evalCase.getCaseId())
            .score(score)
            .passed(score >= 1.0)
            .summary("Latency: " + latencyMs + "ms (threshold: " + maxLatencyMs + "ms, score: "
                + String.format("%.2f", score) + ")")
            .findings(findings)
            .evidence(evidence)
            .build();
    }
}
