package automation.aiEval.evaluator;

import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.EvaluationResult;

import java.util.ArrayList;
import java.util.List;

public class LatencyEvaluator implements AIEvaluator
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
    public EvaluationResult evaluate(AITestCase testCase, AIExecutionResult result)
    {
        List<String> findings = new ArrayList<>();
        List<String> evidence = new ArrayList<>();

        long latencyMs = result.getLatencyMs();
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

        boolean passed = score >= 1.0;

        return EvaluationResult.builder()
            .evaluatorName(getName())
            .caseId(testCase.getCaseId())
            .score(score)
            .passed(passed)
            .summary("Latency: " + latencyMs + "ms (threshold: " + maxLatencyMs + "ms, score: "
                + String.format("%.2f", score) + ")")
            .findings(findings)
            .evidence(evidence)
            .build();
    }
}
