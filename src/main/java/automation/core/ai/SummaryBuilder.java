package automation.core.ai;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryBuilder
{
    public RunSummary build(
        String runId,
        RunContext context,
        List<AggregatedEvaluation> evals,
        List<EvalCase> evalCases)
    {
        int totalCases = evals.size();
        int passedCases = 0;
        double scoreSum = 0.0;
        List<String> failedCaseIds = new ArrayList<>();
        Map<String, Integer> suiteBreakdown = new HashMap<>();
        Map<String, Integer> suitePassBreakdown = new HashMap<>();

        // Build caseId -> EvalCase lookup for suite
        Map<String, EvalCase> caseById = new HashMap<>();
        for (EvalCase evalCase : evalCases)
        {
            if (evalCase.getCaseId() != null)
            {
                caseById.put(evalCase.getCaseId(), evalCase);
            }
        }

        for (AggregatedEvaluation eval : evals)
        {
            scoreSum += eval.getOverallScore();
            if (eval.isFinalPass())
            {
                passedCases++;
            }
            else
            {
                failedCaseIds.add(eval.getCaseId());
            }

            EvalCase evalCase = caseById.get(eval.getCaseId());
            String suite = (evalCase != null && evalCase.getSuite() != null) ? evalCase.getSuite() : "unknown";
            suiteBreakdown.merge(suite, 1, Integer::sum);
            if (eval.isFinalPass())
            {
                suitePassBreakdown.merge(suite, 1, Integer::sum);
            }
            else
            {
                suitePassBreakdown.putIfAbsent(suite, 0);
            }
        }

        int failedCases = totalCases - passedCases;
        double averageScore = totalCases > 0 ? scoreSum / totalCases : 0.0;
        double passRate = totalCases > 0 ? (double) passedCases / totalCases : 0.0;

        return RunSummary.builder()
            .runId(runId)
            .environment(context.getEnvironment())
            .buildVersion(context.getBuildVersion())
            .modelVersion(context.getModelVersion())
            .promptVersion(context.getPromptVersion())
            .timestamp(Instant.now().toString())
            .totalCases(totalCases)
            .passedCases(passedCases)
            .failedCases(failedCases)
            .averageScore(averageScore)
            .passRate(passRate)
            .suiteBreakdown(suiteBreakdown)
            .suitePassBreakdown(suitePassBreakdown)
            .failedCaseIds(failedCaseIds)
            .build();
    }
}
