package automation.core.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * What the AI application returned for one {@link EvalCase}, in the one shape every evaluator reads.
 * Each module's adapter maps its app's reply into this class, so evaluators never see an app's raw format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvalResponse
{
    private String caseId;
    private String output;
    private long latencyMs;
    private double costUsd;
    private List<String> retrievedContexts;
    private boolean success;
    private String errorMessage;
    private String rawResponse;
}
