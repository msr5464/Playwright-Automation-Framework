package automation.aiEval.aggregator;

import automation.aiEval.model.AggregatedEvaluation;
import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.EvaluationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreAggregator
{
    private final AggregationConfig config;

    public ScoreAggregator(AggregationConfig config)
    {
        this.config = config;
    }

    public AggregatedEvaluation aggregate(
        AITestCase testCase,
        AIExecutionResult executionResult,
        List<EvaluationResult> evaluatorResults)
    {
        // Map evaluator name -> score
        Map<String, Double> scoreByName = new HashMap<>();
        for (EvaluationResult er : evaluatorResults)
        {
            scoreByName.put(er.getEvaluatorName(), er.getScore());
        }

        double codeMatchScore = scoreByName.getOrDefault("codeMatch", 1.0);
        double forbiddenScore = scoreByName.getOrDefault("forbiddenCode", 1.0);
        double auditTrailScore = scoreByName.getOrDefault("auditTrail", 1.0);
        double latencyScore = scoreByName.getOrDefault("latency", 1.0);
        double schemaScore = scoreByName.getOrDefault("schema", 1.0);

        // Weighted score using configured weights (rationale + traceability both map to auditTrail)
        double auditWeight = config.getWeightRationale() + config.getWeightTraceability();
        double totalWeight = config.getWeightCodingAccuracy() + config.getWeightSafety()
            + auditWeight + config.getWeightPerformance();
        double overallScore = ((codeMatchScore * config.getWeightCodingAccuracy())
            + (forbiddenScore * config.getWeightSafety())
            + (auditTrailScore * auditWeight)
            + (latencyScore * config.getWeightPerformance())) / totalWeight;

        // Hard fail conditions
        boolean hardFail = (config.isFailOnSchemaError() && schemaScore < 1.0)
            || (config.isFailOnForbiddenCode() && forbiddenScore < 1.0);

        // Threshold based on risk level
        double threshold = "high".equalsIgnoreCase(testCase.getRiskLevel())
            ? config.getHighRiskMinScore()
            : config.getDefaultMinScore();

        boolean finalPass = !hardFail && overallScore >= threshold;

        // Dimension scores map
        Map<String, Double> dimensionScores = new HashMap<>();
        for (EvaluationResult er : evaluatorResults)
        {
            dimensionScores.put(er.getEvaluatorName(), er.getScore());
        }

        // Top issues from all evaluators that failed
        List<String> topIssues = new ArrayList<>();
        for (EvaluationResult er : evaluatorResults)
        {
            if (!er.isPassed() && er.getFindings() != null)
            {
                topIssues.addAll(er.getFindings());
            }
        }

        // riskStatus reflects the test case's risk level, not the pass/fail outcome
        String riskStatus = "high".equalsIgnoreCase(testCase.getRiskLevel()) ? "high-risk" : "standard";

        return AggregatedEvaluation.builder()
            .caseId(testCase.getCaseId())
            .runId(executionResult.getRunId())
            .overallScore(overallScore)
            .finalPass(finalPass)
            .riskStatus(riskStatus)
            .evaluatorResults(evaluatorResults)
            .dimensionScores(dimensionScores)
            .topIssues(topIssues)
            .build();
    }
}
