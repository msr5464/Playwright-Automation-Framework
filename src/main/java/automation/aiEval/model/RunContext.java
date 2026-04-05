package automation.aiEval.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunContext
{
    private String runId;
    private String environment;
    private String baseUrl;
    private String authToken;
    private String buildVersion;
    private String modelVersion;
    private String promptVersion;
    private String knowledgeGraphVersion;
    private Map<String, String> config;
}
