package automation.aiEval.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResult
{
    private String evaluatorName;
    private String caseId;
    private double score;
    private boolean passed;
    private String summary;
    private List<String> findings;
    private List<String> evidence;
}
