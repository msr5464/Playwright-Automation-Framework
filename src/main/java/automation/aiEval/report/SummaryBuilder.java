package automation.aiEval.report;

import automation.aiEval.model.AggregatedEvaluation;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.RunContext;
import automation.aiEval.model.RunSummary;

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
        List<AITestCase> testCases)
    {
        int totalCases = evals.size();
        int passedCases = 0;
        double scoreSum = 0.0;
        List<String> failedCaseIds = new ArrayList<>();

        Map<String, Integer> suiteBreakdown = new HashMap<>();
        Map<String, Integer> suitePassBreakdown = new HashMap<>();
        Map<String, Integer> specialtyBreakdown = new HashMap<>();

        // Build caseId -> AITestCase lookup for specialty
        Map<String, AITestCase> caseById = new HashMap<>();
        for (AITestCase tc : testCases)
        {
            if (tc.getCaseId() != null)
            {
                caseById.put(tc.getCaseId(), tc);
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

            AITestCase tc = caseById.get(eval.getCaseId());
            String suite = (tc != null && tc.getSuite() != null) ? tc.getSuite() : "unknown";
            String specialty = (tc != null && tc.getSpecialty() != null) ? tc.getSpecialty() : "unknown";

            suiteBreakdown.merge(suite, 1, Integer::sum);
            if (eval.isFinalPass())
            {
                suitePassBreakdown.merge(suite, 1, Integer::sum);
            }
            else
            {
                suitePassBreakdown.putIfAbsent(suite, 0);
            }
            specialtyBreakdown.merge(specialty, 1, Integer::sum);
        }

        int failedCases = totalCases - passedCases;
        double averageScore = totalCases > 0 ? scoreSum / totalCases : 0.0;
        double passRate = totalCases > 0 ? (double) passedCases / totalCases : 0.0;

        return RunSummary.builder()
            .runId(runId)
            .environment(context.getEnvironment())
            .buildVersion(context.getBuildVersion())
            .modelVersion(context.getModelVersion())
            .timestamp(Instant.now().toString())
            .totalCases(totalCases)
            .passedCases(passedCases)
            .failedCases(failedCases)
            .averageScore(averageScore)
            .passRate(passRate)
            .suiteBreakdown(suiteBreakdown)
            .suitePassBreakdown(suitePassBreakdown)
            .specialtyBreakdown(specialtyBreakdown)
            .failedCaseIds(failedCaseIds)
            .build();
    }
}
