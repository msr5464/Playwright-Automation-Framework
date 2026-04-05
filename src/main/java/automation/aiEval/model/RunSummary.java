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
public class RunSummary
{
    private String runId;
    private String environment;
    private String buildVersion;
    private String modelVersion;
    private String timestamp;
    private int totalCases;
    private int passedCases;
    private int failedCases;
    private double averageScore;
    private double passRate;
    private Map<String, Integer> suiteBreakdown;
    private Map<String, Integer> suitePassBreakdown;
    private Map<String, Integer> specialtyBreakdown;
    private List<String> failedCaseIds;
}
