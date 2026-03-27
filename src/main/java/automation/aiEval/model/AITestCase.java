package automation.aiEval.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AITestCase
{
    private String caseId;
    private String suite;
    private String domain;
    private String specialty;
    private String severity;
    private String riskLevel;
    private List<String> tags;
    private InputPayload input;
    private ExpectedOutcome expected;
    private ExecutionHints executionHints;
    private Metadata metadata;
}
