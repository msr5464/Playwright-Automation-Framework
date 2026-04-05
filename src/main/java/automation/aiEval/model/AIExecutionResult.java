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
public class AIExecutionResult
{
    private String caseId;
    private String runId;
    private String environment;
    private String buildVersion;
    private String modelVersion;
    private String promptVersion;
    private String knowledgeGraphVersion;
    private Object requestPayload;
    private Object responsePayload;
    private List<String> predictedCodes;
    private String rationale;
    private List<Map<String, Object>> auditTrail;
    private long latencyMs;
    private boolean success;
    private String errorType;
    private String errorMessage;
    private String timestamp;
}
