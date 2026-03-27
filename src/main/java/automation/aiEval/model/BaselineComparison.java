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
public class BaselineComparison
{
    private String baselineRunId;
    private String currentRunId;
    private List<String> newlyFailedCases;
    private List<String> recoveredCases;
    private double scoreDelta;
    private double latencyDelta;
    private List<String> notableChanges;
}
