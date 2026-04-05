package automation.aiEval.runner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawServiceResponse
{
    private int httpStatus;
    private Map<String, String> headers;
    private String body;
    private long latencyMs;
    private Object requestPayload;
}
