package automation.core.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunContext
{
    private String runId;
    private String environment;
    private String buildVersion;
    private String modelVersion;
    private String promptVersion;
}
