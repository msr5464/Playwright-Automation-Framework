package automation.aiEval.normalizer;

import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.RunContext;
import automation.aiEval.runner.RawServiceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DefaultResponseNormalizer implements ResponseNormalizer
{
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public AIExecutionResult normalize(RawServiceResponse raw, AITestCase testCase, RunContext runContext)
    {
        boolean success = raw.getHttpStatus() >= 200 && raw.getHttpStatus() < 300;

        List<String> predictedCodes = new ArrayList<>();
        String rationale = null;
        List<Map<String, Object>> auditTrail = new ArrayList<>();

        Map<String, Object> parsed = null;
        try
        {
            if (raw.getBody() != null && !raw.getBody().isBlank())
            {
                parsed = mapper.readValue(raw.getBody(), Map.class);
            }
        }
        catch (Exception ignored)
        {
            // body is not valid JSON — leave parsed as null
        }

        if (parsed != null)
        {
            predictedCodes = extractCodes(parsed);
            rationale = extractRationale(parsed);
            auditTrail = extractAuditTrail(parsed);
        }

        return AIExecutionResult.builder()
            .caseId(testCase.getCaseId())
            .runId(runContext.getRunId())
            .environment(runContext.getEnvironment())
            .buildVersion(runContext.getBuildVersion())
            .modelVersion(runContext.getModelVersion())
            .promptVersion(runContext.getPromptVersion())
            .knowledgeGraphVersion(runContext.getKnowledgeGraphVersion())
            .requestPayload(raw.getRequestPayload())
            .responsePayload(raw.getBody())
            .predictedCodes(predictedCodes)
            .rationale(rationale)
            .auditTrail(auditTrail)
            .latencyMs(raw.getLatencyMs())
            .success(success)
            .timestamp(java.time.Instant.now().toString())
            .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> extractCodes(Map<String, Object> parsed)
    {
        List<String> codes = new ArrayList<>();
        for (String key : Arrays.asList("predictedCodes", "codes", "results", "predictions"))
        {
            Object value = parsed.get(key);
            if (value == null)
            {
                continue;
            }

            if (value instanceof List)
            {
                List<?> list = (List<?>) value;
                for (Object item : list)
                {
                    if (item instanceof String)
                    {
                        codes.add((String) item);
                    }
                    else if (item instanceof Map)
                    {
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        Object code = itemMap.containsKey("code") ? itemMap.get("code") : itemMap.get("id");
                        if (code != null)
                        {
                            codes.add(code.toString());
                        }
                    }
                }
                if (!codes.isEmpty())
                {
                    return codes;
                }
            }
            else if (value instanceof String[])
            {
                codes.addAll(Arrays.asList((String[]) value));
                if (!codes.isEmpty())
                {
                    return codes;
                }
            }
        }
        return codes;
    }

    private String extractRationale(Map<String, Object> parsed)
    {
        for (String key : Arrays.asList("rationale", "reasoning", "explanation", "justification"))
        {
            Object value = parsed.get(key);
            if (value instanceof String && !((String) value).isBlank())
            {
                return (String) value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractAuditTrail(Map<String, Object> parsed)
    {
        for (String key : Arrays.asList("auditTrail", "audit_trail", "trace", "steps"))
        {
            Object value = parsed.get(key);
            if (value instanceof List)
            {
                List<?> list = (List<?>) value;
                if (!list.isEmpty() && list.get(0) instanceof Map)
                {
                    return (List<Map<String, Object>>) value;
                }
            }
        }
        return new ArrayList<>();
    }
}
