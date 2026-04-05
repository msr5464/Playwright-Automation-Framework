package automation.aiEval.evaluator;

import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.EvaluationResult;
import automation.aiEval.model.ExpectedOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuditTrailEvaluator implements AIEvaluator
{
    @Override
    public String getName()
    {
        return "auditTrail";
    }

    @Override
    public EvaluationResult evaluate(AITestCase testCase, AIExecutionResult result)
    {
        List<String> findings = new ArrayList<>();
        List<String> evidence = new ArrayList<>();

        int totalChecks = 0;
        int passedChecks = 0;

        ExpectedOutcome expected = testCase.getExpected();

        // Check requireAuditTrail
        boolean requireAuditTrail = false;
        if (expected != null && expected.getCustomAssertions() != null)
        {
            Object val = expected.getCustomAssertions().get("requireAuditTrail");
            if (Boolean.TRUE.equals(val) || "true".equalsIgnoreCase(String.valueOf(val)))
            {
                requireAuditTrail = true;
            }
        }

        if (requireAuditTrail)
        {
            totalChecks++;
            if (result.getAuditTrail() != null && !result.getAuditTrail().isEmpty())
            {
                passedChecks++;
                evidence.add("Audit trail present (" + result.getAuditTrail().size() + " entries)");
            }
            else
            {
                findings.add("Audit trail required but missing");
            }
        }

        // Build searchable text from rationale + auditTrail
        String searchableText = buildSearchableText(result.getRationale(), result.getAuditTrail());

        // Check evidenceMustReference
        if (expected != null && expected.getEvidenceMustReference() != null)
        {
            for (String ref : expected.getEvidenceMustReference())
            {
                totalChecks++;
                if (searchableText.toLowerCase().contains(ref.toLowerCase()))
                {
                    passedChecks++;
                    evidence.add("Evidence reference found: '" + ref + "'");
                }
                else
                {
                    findings.add("Evidence reference MISSING: '" + ref + "'");
                }
            }
        }

        // Check rationaleMustContain
        if (expected != null && expected.getRationaleMustContain() != null
                && !expected.getRationaleMustContain().isEmpty())
        {
            for (String phrase : expected.getRationaleMustContain())
            {
                totalChecks++;
                if (result.getRationale() != null
                        && result.getRationale().toLowerCase().contains(phrase.toLowerCase()))
                {
                    passedChecks++;
                    evidence.add("Required phrase found in rationale: '" + phrase + "'");
                }
                else
                {
                    findings.add(result.getRationale() == null
                        ? "Required phrase check failed — rationale is missing"
                        : "Required phrase MISSING from rationale: '" + phrase + "'");
                }
            }
        }

        // Check rationaleMustNotContain
        if (expected != null && expected.getRationaleMustNotContain() != null
                && !expected.getRationaleMustNotContain().isEmpty())
        {
            for (String phrase : expected.getRationaleMustNotContain())
            {
                totalChecks++;
                if (result.getRationale() == null
                        || !result.getRationale().toLowerCase().contains(phrase.toLowerCase()))
                {
                    passedChecks++;
                    evidence.add("Forbidden phrase correctly absent: '" + phrase + "'");
                }
                else
                {
                    findings.add("Forbidden phrase FOUND in rationale: '" + phrase + "'");
                }
            }
        }

        double score = totalChecks == 0 ? 1.0 : (double) passedChecks / totalChecks;
        boolean passed = score >= 1.0;

        return EvaluationResult.builder()
            .evaluatorName(getName())
            .caseId(testCase.getCaseId())
            .score(score)
            .passed(passed)
            .summary("Audit trail score: " + String.format("%.2f", score)
                + " (" + passedChecks + "/" + totalChecks + " checks passed)")
            .findings(findings)
            .evidence(evidence)
            .build();
    }

    private String buildSearchableText(String rationale, List<Map<String, Object>> auditTrail)
    {
        StringBuilder sb = new StringBuilder();
        if (rationale != null)
        {
            sb.append(rationale).append(" ");
        }
        if (auditTrail != null)
        {
            for (Map<String, Object> entry : auditTrail)
            {
                sb.append(entry.toString()).append(" ");
            }
        }
        return sb.toString();
    }
}
