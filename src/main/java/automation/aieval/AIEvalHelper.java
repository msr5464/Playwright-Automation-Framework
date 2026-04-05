package automation.aiEval;

import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for AI Evaluation test cases.
 * Extends ApiHelper to use existing Jarvis2 API execution and integrates
 * AI-specific evaluation logic: code matching, forbidden codes, latency monitoring,
 * audit trail validation, and schema validation.
 *
 * Scoring dimensions and default weights:
 *   Accuracy (40%) + Safety (20%) + Rationale (15%) + Traceability (15%) + Performance (10%)
 *
 * Configure via Parameters/ai-eval.properties (falls back to defaults if absent).
 */
public class AIEvalHelper extends ApiHelper
{
    // Evaluation thresholds (loaded from ai-eval.properties)
    private double defaultMinScore = 0.85;
    private double highRiskMinScore = 0.95;
    private long maxLatencyMs = 8000;
    private boolean failOnSchemaError = true;
    private boolean failOnForbiddenCode = true;

    // Dimension weights
    private double weightCodingAccuracy = 0.40;
    private double weightSafety = 0.20;
    private double weightTraceability = 0.15;
    private double weightPerformance = 0.10;

    public AIEvalHelper(Config testConfig)
    {
        super(testConfig);
        loadEvalConfig();
    }

    /**
     * Load evaluation config from Parameters/ai-eval.properties.
     * Falls back to built-in defaults if the file is not found.
     */
    private void loadEvalConfig()
    {
        String propsPath = System.getProperty("user.dir") + File.separator + "parameters" + File.separator + "ai-eval.properties";
        try (FileInputStream fis = new FileInputStream(propsPath))
        {
            Properties props = new Properties();
            props.load(fis);

            defaultMinScore    = Double.parseDouble(props.getProperty("aiEval.defaultMinScore",    "0.85"));
            highRiskMinScore   = Double.parseDouble(props.getProperty("aiEval.highRiskMinScore",   "0.95"));
            maxLatencyMs       = Long.parseLong(    props.getProperty("aiEval.maxLatencyMs",       "8000"));
            failOnSchemaError  = Boolean.parseBoolean(props.getProperty("aiEval.failOnSchemaError",  "true"));
            failOnForbiddenCode = Boolean.parseBoolean(props.getProperty("aiEval.failOnForbiddenCode","true"));

            weightCodingAccuracy = Double.parseDouble(props.getProperty("aiEval.weight.codingAccuracy", "0.40"));
            weightSafety         = Double.parseDouble(props.getProperty("aiEval.weight.safety",          "0.20"));
            weightTraceability   = Double.parseDouble(props.getProperty("aiEval.weight.traceability",    "0.15"));
            weightPerformance    = Double.parseDouble(props.getProperty("aiEval.weight.performance",     "0.10"));

            Log.comment(config, "AI Eval config loaded from: " + propsPath);
        }
        catch (Exception e)
        {
            Log.comment(config, "Using default AI Eval config (ai-eval.properties not found at " + propsPath + ")");
        }
    }

    /**
     * Main evaluation method: runs all evaluators against the AI response and
     * logs results via Log.pass / Log.fail.
     * Returns the overall weighted score (0.0 – 1.0).
     */
    public double evaluateAIResponse(Response response)
    {
        String caseId    = config.testData.getOrDefault("caseId", "unknown");
        String riskLevel = config.testData.getOrDefault("riskLevel", "low");
        String severity  = config.testData.getOrDefault("severity", "normal");
        long latencyMs   = response.getTime();

        Log.comment(config, "--- AI Evaluation for case: " + caseId + " (risk: " + riskLevel + ", severity: " + severity + ") ---");

        // Parse response
        List<String> predictedCodes = parsePredictedCodes(response);
        String rationale = parseRationale(response);
        List<Map<String, Object>> auditTrail = parseAuditTrail(response);

        Log.comment(config, "Predicted codes: " + predictedCodes);
        Log.comment(config, "Latency: " + latencyMs + "ms");

        // Run all evaluators
        double schemaScore       = evaluateSchema(response, predictedCodes, rationale, auditTrail);
        double codeMatchScore    = evaluateCodeMatch(predictedCodes);
        double forbiddenCodeScore = evaluateForbiddenCodes(predictedCodes);
        double latencyScore      = evaluateLatency(latencyMs);
        double auditTrailScore   = evaluateAuditTrail(rationale, auditTrail);

        // Compute weighted score
        double overallScore = computeWeightedScore(codeMatchScore, forbiddenCodeScore, auditTrailScore, latencyScore);

        // Determine pass/fail
        double minScore = "high".equalsIgnoreCase(riskLevel) ? highRiskMinScore : defaultMinScore;
        boolean hardFail = (failOnSchemaError && schemaScore < 1.0) || (failOnForbiddenCode && forbiddenCodeScore < 1.0);
        boolean passed = !hardFail && overallScore >= minScore;

        // Log summary
        Log.comment(config, "--- Evaluation Summary ---");
        Log.comment(config, "Schema: " + formatPct(schemaScore)
                + " | CodeMatch: " + formatPct(codeMatchScore)
                + " | Safety: " + formatPct(forbiddenCodeScore)
                + " | Latency: " + formatPct(latencyScore)
                + " | AuditTrail: " + formatPct(auditTrailScore));
        Log.comment(config, "Overall score: " + formatPct(overallScore) + " (threshold: " + formatPct(minScore) + ")");

        if (hardFail)
        {
            Log.fail(config, "HARD FAIL for case " + caseId + " - schema or forbidden code violation");
        }
        else if (passed)
        {
            Log.pass(config, "AI Evaluation PASSED for case " + caseId + " with score " + formatPct(overallScore));
        }
        else
        {
            Log.fail(config, "AI Evaluation FAILED for case " + caseId
                    + " - score " + formatPct(overallScore) + " below threshold " + formatPct(minScore));
        }

        return overallScore;
    }

    // ===== Evaluator: Schema Validation =====

    private double evaluateSchema(Response response, List<String> predictedCodes, String rationale,
                                  List<Map<String, Object>> auditTrail)
    {
        boolean passed = true;

        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300)
        {
            Log.fail(config, "[Schema] API returned HTTP " + response.getStatusCode());
            passed = false;
        }

        if (predictedCodes.isEmpty())
        {
            Log.fail(config, "[Schema] No predicted codes in response");
            passed = false;
        }
        else
        {
            Log.pass(config, "[Schema] Predicted codes present: " + predictedCodes.size() + " code(s)");
        }

        String mustContain = config.testData.getOrDefault("rationaleMustContain", "");
        if (StringUtils.isNotEmpty(mustContain) && StringUtils.isEmpty(rationale))
        {
            Log.fail(config, "[Schema] Rationale is required but missing");
            passed = false;
        }

        boolean auditRequired = "true".equalsIgnoreCase(config.testData.getOrDefault("requireAuditTrail", "false"));
        if (auditRequired && (auditTrail == null || auditTrail.isEmpty()))
        {
            Log.fail(config, "[Schema] Audit trail is required but missing");
            passed = false;
        }

        return passed ? 1.0 : 0.0;
    }

    // ===== Evaluator: Code Match =====

    private double evaluateCodeMatch(List<String> predictedCodes)
    {
        List<String> expectedCodes   = parsePipeDelimited(config.testData.getOrDefault("expectedCodes", ""));
        List<String> allowedAltCodes = parsePipeDelimited(config.testData.getOrDefault("allowedAltCodes", ""));

        if (expectedCodes.isEmpty())
        {
            Log.pass(config, "[CodeMatch] No expected codes defined - skipping");
            return 1.0;
        }

        Set<String> acceptable = new HashSet<>(expectedCodes);
        acceptable.addAll(allowedAltCodes);

        int matchedCount = 0;
        for (String expected : expectedCodes)
        {
            if (predictedCodes.contains(expected))
            {
                matchedCount++;
                Log.pass(config, "[CodeMatch] Expected code matched: " + expected);
            }
            else
            {
                boolean altFound = allowedAltCodes.stream().anyMatch(predictedCodes::contains);
                if (altFound)
                {
                    matchedCount++;
                    Log.pass(config, "[CodeMatch] Expected code " + expected + " matched via allowed alternative");
                }
                else
                {
                    Log.fail(config, "[CodeMatch] Expected code NOT found: " + expected);
                }
            }
        }

        List<String> unexpected = predictedCodes.stream()
                .filter(c -> !acceptable.contains(c))
                .collect(Collectors.toList());
        if (!unexpected.isEmpty())
        {
            Log.comment(config, "[CodeMatch] Unexpected codes predicted: " + unexpected);
        }

        return (double) matchedCount / expectedCodes.size();
    }

    // ===== Evaluator: Forbidden Codes =====

    private double evaluateForbiddenCodes(List<String> predictedCodes)
    {
        List<String> forbiddenCodes = parsePipeDelimited(config.testData.getOrDefault("forbiddenCodes", ""));

        if (forbiddenCodes.isEmpty())
        {
            Log.pass(config, "[Safety] No forbidden codes defined - skipping");
            return 1.0;
        }

        List<String> violations = predictedCodes.stream()
                .filter(forbiddenCodes::contains)
                .collect(Collectors.toList());

        if (violations.isEmpty())
        {
            Log.pass(config, "[Safety] No forbidden codes detected");
            return 1.0;
        }
        else
        {
            Log.fail(config, "[Safety] FORBIDDEN codes detected: " + violations);
            String severity = config.testData.getOrDefault("severity", "normal");
            if ("critical".equalsIgnoreCase(severity))
            {
                Log.fail(config, "[Safety] CRITICAL: Forbidden codes in critical-severity case");
            }
            return 0.0;
        }
    }

    // ===== Evaluator: Latency =====

    private double evaluateLatency(long latencyMs)
    {
        if (latencyMs <= maxLatencyMs)
        {
            Log.pass(config, "[Performance] Latency " + latencyMs + "ms within threshold (" + maxLatencyMs + "ms)");
            return 1.0;
        }
        else
        {
            Log.fail(config, "[Performance] Latency " + latencyMs + "ms EXCEEDS threshold (" + maxLatencyMs + "ms)");
            if (latencyMs >= maxLatencyMs * 2) return 0.0;
            return 1.0 - ((double) (latencyMs - maxLatencyMs) / maxLatencyMs);
        }
    }

    // ===== Evaluator: Audit Trail & Rationale =====

    private double evaluateAuditTrail(String rationale, List<Map<String, Object>> auditTrail)
    {
        int checks = 0;
        int passedChecks = 0;

        boolean auditRequired = "true".equalsIgnoreCase(config.testData.getOrDefault("requireAuditTrail", "false"));
        if (auditRequired)
        {
            checks++;
            if (auditTrail != null && !auditTrail.isEmpty())
            {
                passedChecks++;
                Log.pass(config, "[Traceability] Audit trail present (" + auditTrail.size() + " entries)");
            }
            else
            {
                Log.fail(config, "[Traceability] Audit trail required but missing");
            }
        }

        String searchableText = buildSearchableText(rationale, auditTrail);

        List<String> evidenceRefs = parsePipeDelimited(config.testData.getOrDefault("evidenceMustReference", ""));
        for (String ref : evidenceRefs)
        {
            checks++;
            if (searchableText.toLowerCase().contains(ref.toLowerCase()))
            {
                passedChecks++;
                Log.pass(config, "[Traceability] Evidence reference found: '" + ref + "'");
            }
            else
            {
                Log.fail(config, "[Traceability] Evidence reference MISSING: '" + ref + "'");
            }
        }

        List<String> mustContain = parsePipeDelimited(config.testData.getOrDefault("rationaleMustContain", ""));
        if (rationale != null)
        {
            for (String phrase : mustContain)
            {
                checks++;
                if (rationale.toLowerCase().contains(phrase.toLowerCase()))
                {
                    passedChecks++;
                    Log.pass(config, "[Rationale] Required phrase found: '" + phrase + "'");
                }
                else
                {
                    Log.fail(config, "[Rationale] Required phrase MISSING: '" + phrase + "'");
                }
            }
        }

        List<String> mustNotContain = parsePipeDelimited(config.testData.getOrDefault("rationaleMustNotContain", ""));
        if (rationale != null)
        {
            for (String phrase : mustNotContain)
            {
                checks++;
                if (!rationale.toLowerCase().contains(phrase.toLowerCase()))
                {
                    passedChecks++;
                    Log.pass(config, "[Rationale] Forbidden phrase correctly absent: '" + phrase + "'");
                }
                else
                {
                    Log.fail(config, "[Rationale] Forbidden phrase FOUND: '" + phrase + "'");
                }
            }
        }

        if (checks == 0) return 1.0;
        return (double) passedChecks / checks;
    }

    // ===== Weighted Score =====

    private double computeWeightedScore(double codeMatch, double safety, double auditTrail, double latency)
    {
        double totalWeight = weightCodingAccuracy + weightSafety + weightTraceability + weightPerformance;
        double weightedSum = (weightCodingAccuracy * codeMatch)
                + (weightSafety * safety)
                + (weightTraceability * auditTrail)
                + (weightPerformance * latency);
        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }

    // ===== Response Parsing =====

    @SuppressWarnings("unchecked")
    private List<String> parsePredictedCodes(Response response)
    {
        List<String> codes = new ArrayList<>();
        try
        {
            for (String key : Arrays.asList("predictedCodes", "codes", "results", "predictions"))
            {
                try
                {
                    List<Object> items = response.jsonPath().getList(key);
                    if (items != null && !items.isEmpty())
                    {
                        for (Object item : items)
                        {
                            if (item instanceof String)
                            {
                                codes.add((String) item);
                            }
                            else if (item instanceof Map)
                            {
                                Map<String, Object> map = (Map<String, Object>) item;
                                Object code = map.getOrDefault("code", map.get("id"));
                                if (code != null) codes.add(code.toString());
                            }
                        }
                        if (!codes.isEmpty()) return codes;
                    }
                }
                catch (Exception ignored) { }
            }
        }
        catch (Exception e)
        {
            Log.comment(config, "Could not parse predicted codes: " + e.getMessage());
        }
        return codes;
    }

    private String parseRationale(Response response)
    {
        for (String key : Arrays.asList("rationale", "reasoning", "explanation", "justification"))
        {
            try
            {
                String value = response.jsonPath().getString(key);
                if (StringUtils.isNotEmpty(value)) return value;
            }
            catch (Exception ignored) { }
        }
        return null;
    }

    private List<Map<String, Object>> parseAuditTrail(Response response)
    {
        for (String key : Arrays.asList("auditTrail", "audit_trail", "trace", "steps"))
        {
            try
            {
                List<Map<String, Object>> trail = response.jsonPath().getList(key);
                if (trail != null && !trail.isEmpty()) return trail;
            }
            catch (Exception ignored) { }
        }
        return new ArrayList<>();
    }

    // ===== Utilities =====

    private List<String> parsePipeDelimited(String value)
    {
        if (StringUtils.isEmpty(value)) return new ArrayList<>();
        return Arrays.stream(value.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String buildSearchableText(String rationale, List<Map<String, Object>> auditTrail)
    {
        StringBuilder sb = new StringBuilder();
        if (rationale != null) sb.append(rationale).append(" ");
        if (auditTrail != null)
        {
            for (Map<String, Object> entry : auditTrail)
            {
                sb.append(entry.toString()).append(" ");
            }
        }
        return sb.toString();
    }

    private String formatPct(double score)
    {
        return String.format("%.1f%%", score * 100);
    }
}
