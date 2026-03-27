package automation.aiEval.aggregator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Properties;

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

    @Builder.Default
    private boolean failOnSchemaError = true;

    @Builder.Default
    private boolean failOnForbiddenCode = true;

    @Builder.Default
    private double weightCodingAccuracy = 0.40;

    @Builder.Default
    private double weightSafety = 0.20;

    @Builder.Default
    private double weightRationale = 0.15;

    @Builder.Default
    private double weightTraceability = 0.15;

    @Builder.Default
    private double weightPerformance = 0.10;

    public static AggregationConfig fromProperties(Properties props)
    {
        return AggregationConfig.builder()
            .defaultMinScore(Double.parseDouble(props.getProperty("aiEval.defaultMinScore", "0.85")))
            .highRiskMinScore(Double.parseDouble(props.getProperty("aiEval.highRiskMinScore", "0.95")))
            .failOnSchemaError(Boolean.parseBoolean(props.getProperty("aiEval.failOnSchemaError", "true")))
            .failOnForbiddenCode(Boolean.parseBoolean(props.getProperty("aiEval.failOnForbiddenCode", "true")))
            .weightCodingAccuracy(Double.parseDouble(props.getProperty("aiEval.weight.codingAccuracy", "0.40")))
            .weightSafety(Double.parseDouble(props.getProperty("aiEval.weight.safety", "0.20")))
            .weightRationale(Double.parseDouble(props.getProperty("aiEval.weight.rationale", "0.15")))
            .weightTraceability(Double.parseDouble(props.getProperty("aiEval.weight.traceability", "0.15")))
            .weightPerformance(Double.parseDouble(props.getProperty("aiEval.weight.performance", "0.10")))
            .build();
    }
}
