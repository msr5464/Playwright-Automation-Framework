package automation.aiEval.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpectedOutcome
{
    private List<String> expectedCodes;
    private List<String> allowedAlternativeCodes;
    private List<String> forbiddenCodes;
    private List<String> rationaleMustContain;
    private List<String> rationaleMustNotContain;
    private List<String> evidenceMustReference;
    private Map<String, Object> customAssertions;
}
