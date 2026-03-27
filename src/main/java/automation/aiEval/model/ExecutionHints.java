package automation.aiEval.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionHints
{
    @Builder.Default
    private long timeoutMs = 45000;
    @Builder.Default
    private boolean asyncFlow = false;
    @Builder.Default
    private long pollingIntervalMs = 2000;
    @Builder.Default
    private int maxPollingAttempts = 10;
    private String targetWorkflow;
}
