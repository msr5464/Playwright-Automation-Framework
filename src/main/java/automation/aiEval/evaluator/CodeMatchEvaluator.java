package automation.aiEval.evaluator;

import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.EvaluationResult;
import automation.aiEval.model.ExpectedOutcome;

import java.util.ArrayList;
import java.util.List;

public class CodeMatchEvaluator implements AIEvaluator
{
    @Override
    public String getName()
    {
        return "codeMatch";
    }

    @Override
    public EvaluationResult evaluate(AITestCase testCase, AIExecutionResult result)
    {
        List<String> findings = new ArrayList<>();
        List<String> evidence = new ArrayList<>();

        ExpectedOutcome expected = testCase.getExpected();
        if (expected == null
            || expected.getExpectedCodes() == null
            || expected.getExpectedCodes().isEmpty())
        {
            return EvaluationResult.builder()
                .evaluatorName(getName())
                .caseId(testCase.getCaseId())
                .score(1.0)
                .passed(true)
                .summary("No expected codes defined")
                .findings(findings)
                .evidence(evidence)
                .build();
        }

        List<String> expectedCodes = expected.getExpectedCodes();
        List<String> allowedAlts = expected.getAllowedAlternativeCodes() != null
            ? expected.getAllowedAlternativeCodes()
            : new ArrayList<>();
        List<String> predictedCodes = result.getPredictedCodes() != null
            ? result.getPredictedCodes()
            : new ArrayList<>();

        int matchedCount = 0;
        for (String expectedCode : expectedCodes)
        {
            if (predictedCodes.contains(expectedCode))
            {
                matchedCount++;
                evidence.add("Expected code matched: " + expectedCode);
            }
            else
            {
                boolean altFound = false;
                for (String alt : allowedAlts)
                {
                    if (predictedCodes.contains(alt))
                    {
                        altFound = true;
                        evidence.add("Expected code " + expectedCode + " matched via allowed alternative: " + alt);
                        break;
                    }
                }
                if (altFound)
                {
                    matchedCount++;
                }
                else
                {
                    findings.add("Expected code NOT found: " + expectedCode);
                }
            }
        }

        double score = (double) matchedCount / expectedCodes.size();
        boolean passed = score == 1.0;

        return EvaluationResult.builder()
            .evaluatorName(getName())
            .caseId(testCase.getCaseId())
            .score(score)
            .passed(passed)
            .summary("Code match score: " + String.format("%.2f", score)
                + " (" + matchedCount + "/" + expectedCodes.size() + " matched)")
            .findings(findings)
            .evidence(evidence)
            .build();
    }
}
