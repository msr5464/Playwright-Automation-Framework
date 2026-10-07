package automation.core.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks the output contains every phrase in {@link EvalCase#getMustContain()}, ignoring case.
 * Score is the share of phrases found.
 */
public class MustContainEvaluator implements Evaluator
{
    @Override
    public String getName()
    {
        return "mustContain";
    }

    @Override
    public EvaluationResult evaluate(EvalCase evalCase, EvalResponse response)
    {
        List<String> findings = new ArrayList<>();
        List<String> evidence = new ArrayList<>();
        List<String> phrases = evalCase.getMustContain();

        if (phrases == null || phrases.isEmpty())
        {
            return EvaluationResult.builder()
                .evaluatorName(getName())
                .caseId(evalCase.getCaseId())
                .score(1.0)
                .passed(true)
                .summary("No required phrases defined")
                .findings(findings)
                .evidence(evidence)
                .build();
        }

        String output = response.getOutput() == null ? "" : response.getOutput().toLowerCase();
        int found = 0;
        for (String phrase : phrases)
        {
            if (output.contains(phrase.toLowerCase()))
            {
                found++;
                evidence.add("Required phrase found: '" + phrase + "'");
            }
            else
            {
                findings.add("Required phrase MISSING: '" + phrase + "'");
            }
        }

        return EvaluationResult.builder()
            .evaluatorName(getName())
            .caseId(evalCase.getCaseId())
            .score((double) found / phrases.size())
            .passed(found == phrases.size())
            .summary("Required phrases: " + found + "/" + phrases.size() + " found")
            .findings(findings)
            .evidence(evidence)
            .build();
    }
}
