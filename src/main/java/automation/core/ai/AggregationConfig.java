package automation.core.ai;

import automation.core.Config;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregationConfig
{
    @Builder.Default
    private double defaultMinScore = 0.85;

    @Builder.Default
    private double highRiskMinScore = 0.95;

    // Evaluator name -> weight. Evaluators not listed weigh 1.0, so a new evaluator always counts.
    @Builder.Default
    private Map<String, Double> weights = new HashMap<>();

    // A failure from any of these fails the case whatever its score — the "never" checks.
    @Builder.Default
    private Set<String> hardFailEvaluators = new HashSet<>(Set.of("mustNotContain"));

    /** Thresholds from the properties files (ai.eval.minScore, ai.eval.highRiskMinScore), defaults otherwise. */
    public static AggregationConfig fromConfig(Config config)
    {
        return AggregationConfig.builder()
            .defaultMinScore(Double.parseDouble(config.getRunTimeProperty("ai.eval.minScore", "0.85")))
            .highRiskMinScore(Double.parseDouble(config.getRunTimeProperty("ai.eval.highRiskMinScore", "0.95")))
            .build();
    }
}
