package automation.aiEval.evaluator;

import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.EvaluationResult;
import automation.aiEval.model.ExpectedOutcome;

import java.util.ArrayList;
import java.util.List;

public class ForbiddenCodeEvaluator implements AIEvaluator
{
    @Override
    public String getName()
    {
        return "forbiddenCode";
    }

    @Override
    public EvaluationResult evaluate(AITestCase testCase, AIExecutionResult result)
    {
        List<String> findings = new ArrayList<>();
        List<String> evidence = new ArrayList<>();

        ExpectedOutcome expected = testCase.getExpected();
        if (expected == null
            || expected.getForbiddenCodes() == null
            || expected.getForbiddenCodes().isEmpty())
        {
            return EvaluationResult.builder()
                .evaluatorName(getName())
                .caseId(testCase.getCaseId())
                .score(1.0)
                .passed(true)
                .summary("No forbidden codes defined")
                .findings(findings)
                .evidence(evidence)
                .build();
        }

        List<String> forbiddenCodes = expected.getForbiddenCodes();
        List<String> predictedCodes = result.getPredictedCodes() != null
            ? result.getPredictedCodes()
            : new ArrayList<>();

        List<String> violations = new ArrayList<>();
        for (String predicted : predictedCodes)
        {
            if (forbiddenCodes.contains(predicted))
            {
                violations.add(predicted);
            }
        }

        if (violations.isEmpty())
        {
            evidence.add("No forbidden codes detected");
            return EvaluationResult.builder()
                .evaluatorName(getName())
                .caseId(testCase.getCaseId())
                .score(1.0)
                .passed(true)
                .summary("No forbidden codes violated")
                .findings(findings)
                .evidence(evidence)
                .build();
        }

        findings.add("Forbidden codes detected: " + violations);

        if ("critical".equalsIgnoreCase(testCase.getSeverity()))
        {
            findings.add("CRITICAL severity case with forbidden code violation");
        }

        return EvaluationResult.builder()
            .evaluatorName(getName())
            .caseId(testCase.getCaseId())
            .score(0.0)
            .passed(false)
            .summary("Forbidden code violations detected: " + violations)
            .findings(findings)
            .evidence(evidence)
            .build();
    }
}
