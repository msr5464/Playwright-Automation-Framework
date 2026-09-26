package automation.core.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns one case's evaluator results into a verdict. A case passes only if the call succeeded, at least one
 * evaluator ran, no hard-fail evaluator failed, and the weighted score of every evaluator reaches the
 * threshold for the case's risk level.
 */
public class ScoreAggregator
{
    private final AggregationConfig config;

    public ScoreAggregator(AggregationConfig config)
    {
        this.config = config;
    }

    public AggregatedEvaluation aggregate(
        EvalCase evalCase,
        EvalResponse response,
        List<EvaluationResult> evaluatorResults)
    {
        List<String> topIssues = new ArrayList<>();
        if (!response.isSuccess())
        {
            topIssues.add("Call failed: " + response.getErrorMessage());
        }
        if (evaluatorResults.isEmpty())
        {
            topIssues.add("No evaluators ran");
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;
        boolean hardFail = false;
        Map<String, Double> dimensionScores = new HashMap<>();
        for (EvaluationResult er : evaluatorResults)
        {
            double weight = config.getWeights().getOrDefault(er.getEvaluatorName(), 1.0);
            weightedSum += er.getScore() * weight;
            totalWeight += weight;
            dimensionScores.put(er.getEvaluatorName(), er.getScore());

            if (!er.isPassed())
            {
                if (er.getFindings() != null)
                {
                    topIssues.addAll(er.getFindings());
                }
                if (config.getHardFailEvaluators().contains(er.getEvaluatorName()))
                {
                    hardFail = true;
                }
            }
        }
        double overallScore = totalWeight > 0 ? weightedSum / totalWeight : 0.0;

        // riskStatus reflects the case's risk level, not the pass/fail outcome
        boolean highRisk = "high".equalsIgnoreCase(evalCase.getRiskLevel());
        double threshold = highRisk ? config.getHighRiskMinScore() : config.getDefaultMinScore();
        boolean finalPass = response.isSuccess()
            && !evaluatorResults.isEmpty()
            && !hardFail
            && overallScore >= threshold;

        return AggregatedEvaluation.builder()
            .caseId(evalCase.getCaseId())
            .overallScore(overallScore)
            .finalPass(finalPass)
            .riskStatus(highRisk ? "high-risk" : "standard")
            .evaluatorResults(evaluatorResults)
            .dimensionScores(dimensionScores)
            .topIssues(topIssues)
            .build();
    }
}
