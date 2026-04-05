package automation.aiEval.evaluator;

import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.EvaluationResult;

import java.util.ArrayList;
import java.util.List;

public class SchemaValidatorEvaluator implements AIEvaluator
{
    @Override
    public String getName()
    {
        return "schema";
    }

    @Override
    public EvaluationResult evaluate(AITestCase testCase, AIExecutionResult result)
    {
        List<String> findings = new ArrayList<>();
        List<String> evidence = new ArrayList<>();
        boolean allPass = true;

        if (!result.isSuccess())
        {
            findings.add("Execution was not successful");
            allPass = false;
        }
        else
        {
            evidence.add("Execution succeeded");
        }

        if (result.getPredictedCodes() == null || result.getPredictedCodes().isEmpty())
        {
            findings.add("No predicted codes in response");
            allPass = false;
        }
        else
        {
            evidence.add("Predicted codes present: " + result.getPredictedCodes().size() + " code(s)");
        }

        if (testCase.getExpected() != null
            && testCase.getExpected().getRationaleMustContain() != null
            && !testCase.getExpected().getRationaleMustContain().isEmpty())
        {
            if (result.getRationale() == null || result.getRationale().isBlank())
            {
                findings.add("Rationale is required but missing");
                allPass = false;
            }
            else
            {
                evidence.add("Rationale is present");
            }
        }

        boolean requireAuditTrail = false;
        if (testCase.getExpected() != null
            && testCase.getExpected().getCustomAssertions() != null)
        {
            Object val = testCase.getExpected().getCustomAssertions().get("requireAuditTrail");
            if (Boolean.TRUE.equals(val) || "true".equalsIgnoreCase(String.valueOf(val)))
            {
                requireAuditTrail = true;
            }
        }

        if (requireAuditTrail)
        {
            if (result.getAuditTrail() == null || result.getAuditTrail().isEmpty())
            {
                findings.add("Audit trail is required but missing");
                allPass = false;
            }
            else
            {
                evidence.add("Audit trail present");
            }
        }

        double score = allPass ? 1.0 : 0.0;

        return EvaluationResult.builder()
            .evaluatorName(getName())
            .caseId(testCase.getCaseId())
            .score(score)
            .passed(allPass)
            .summary(allPass ? "Schema validation passed" : "Schema validation failed")
            .findings(findings)
            .evidence(evidence)
            .build();
    }
}
