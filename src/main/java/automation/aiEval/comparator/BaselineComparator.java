package automation.aiEval.comparator;

import automation.aiEval.model.AggregatedEvaluation;
import automation.aiEval.model.BaselineComparison;
import automation.aiEval.model.RunSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaselineComparator
{
    public BaselineComparison compare(
        RunSummary baseline,
        RunSummary current,
        List<AggregatedEvaluation> baselineEvals,
        List<AggregatedEvaluation> currentEvals)
    {
        // Build pass/fail maps
        Map<String, Boolean> baselinePassMap = new HashMap<>();
        for (AggregatedEvaluation eval : baselineEvals)
        {
            baselinePassMap.put(eval.getCaseId(), eval.isFinalPass());
        }

        Map<String, Boolean> currentPassMap = new HashMap<>();
        for (AggregatedEvaluation eval : currentEvals)
        {
            currentPassMap.put(eval.getCaseId(), eval.isFinalPass());
        }

        List<String> newlyFailed = new ArrayList<>();
        List<String> recovered = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : currentPassMap.entrySet())
        {
            String caseId = entry.getKey();
            boolean currentPass = entry.getValue();
            Boolean baselinePass = baselinePassMap.get(caseId);

            if (baselinePass == null)
            {
                continue;
            }

            if (baselinePass && !currentPass)
            {
                newlyFailed.add(caseId);
            }
            else if (!baselinePass && currentPass)
            {
                recovered.add(caseId);
            }
        }

        double scoreDelta = current.getAverageScore() - baseline.getAverageScore();

        // Compute latency delta: not directly in RunSummary, set to 0
        double latencyDelta = 0.0;

        List<String> notableChanges = new ArrayList<>();

        double scorePercent = Math.abs(scoreDelta * 100);
        if (scoreDelta > 0)
        {
            notableChanges.add(String.format("Score improved by %.1f%%", scorePercent));
        }
        else if (scoreDelta < 0)
        {
            notableChanges.add(String.format("Score degraded by %.1f%%", scorePercent));
        }

        if (!newlyFailed.isEmpty())
        {
            notableChanges.add(newlyFailed.size() + " case(s) newly failing");
        }

        if (!recovered.isEmpty())
        {
            notableChanges.add(recovered.size() + " case(s) recovered");
        }

        int passRateDiff = current.getPassedCases() - baseline.getPassedCases();
        if (passRateDiff > 0)
        {
            notableChanges.add(passRateDiff + " more case(s) passing than baseline");
        }
        else if (passRateDiff < 0)
        {
            notableChanges.add(Math.abs(passRateDiff) + " fewer case(s) passing than baseline");
        }

        return BaselineComparison.builder()
            .baselineRunId(baseline.getRunId())
            .currentRunId(current.getRunId())
            .newlyFailedCases(newlyFailed)
            .recoveredCases(recovered)
            .scoreDelta(scoreDelta)
            .latencyDelta(latencyDelta)
            .notableChanges(notableChanges)
            .build();
    }
}
