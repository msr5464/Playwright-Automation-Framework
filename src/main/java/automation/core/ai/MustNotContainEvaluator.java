package automation.core.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * The "never" check: fails if the output contains any phrase in {@link EvalCase#getMustNotContain()},
 * ignoring case. {@link AggregationConfig} hard-fails the case on it by default.
 */
public class MustNotContainEvaluator implements Evaluator
{
    @Override
    public String getName()
    {
        return "mustNotContain";
    }

    @Override
    public EvaluationResult evaluate(EvalCase evalCase, EvalResponse response)
    {
        List<String> findings = new ArrayList<>();
        List<String> evidence = new ArrayList<>();
        String output = response.getOutput() == null ? "" : response.getOutput().toLowerCase();

        if (evalCase.getMustNotContain() != null)
        {
            for (String phrase : evalCase.getMustNotContain())
            {
                if (output.contains(phrase.toLowerCase()))
                {
                    findings.add("Forbidden phrase FOUND: '" + phrase + "'");
                }
                else
                {
                    evidence.add("Forbidden phrase absent: '" + phrase + "'");
                }
            }
        }

        boolean passed = findings.isEmpty();
        return EvaluationResult.builder()
            .evaluatorName(getName())
            .caseId(evalCase.getCaseId())
            .score(passed ? 1.0 : 0.0)
            .passed(passed)
            .summary(passed ? "No forbidden phrases found" : findings.size() + " forbidden phrase(s) found")
            .findings(findings)
            .evidence(evidence)
            .build();
    }
}
