package automation.aiEval.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedEvaluation
{
    private String caseId;
    private String runId;
    private double overallScore;
    private boolean finalPass;
    private String riskStatus;
    private List<EvaluationResult> evaluatorResults;
    private Map<String, Double> dimensionScores;
    private List<String> topIssues;
}
