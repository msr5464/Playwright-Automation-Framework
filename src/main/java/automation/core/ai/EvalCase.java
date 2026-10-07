package automation.core.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * One row of an evaluation dataset: what to send to the AI application and what its reply must satisfy.
 * App-agnostic — each module's adapter turns {@code input} into a call to its own application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvalCase
{
    private String caseId;
    private String suite;
    private List<String> tags;
    private String riskLevel;
    private String input;
    private List<String> mustContain;
    private List<String> mustNotContain;
    private Metadata metadata;
}
