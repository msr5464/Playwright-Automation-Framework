package automation.core.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks what one call cost against a budget. Binary: within budget scores 1.0, over it scores 0.0.
 */
public class CostEvaluator implements Evaluator
{
    private final double maxCostUsd;

    public CostEvaluator(double maxCostUsd)
    {
        this.maxCostUsd = maxCostUsd;
    }

    @Override
    public String getName()
    {
        return "cost";
    }

    @Override
    public EvaluationResult evaluate(EvalCase evalCase, EvalResponse response)
    {
        List<String> findings = new ArrayList<>();
        List<String> evidence = new ArrayList<>();
        String cost = String.format("$%.4f", response.getCostUsd());
        String budget = String.format("$%.4f", maxCostUsd);
        boolean passed = response.getCostUsd() <= maxCostUsd;

        if (passed)
        {
            evidence.add("Cost " + cost + " within budget (" + budget + ")");
        }
        else
        {
            findings.add("Cost " + cost + " exceeds budget (" + budget + ")");
        }

        return EvaluationResult.builder()
            .evaluatorName(getName())
            .caseId(evalCase.getCaseId())
            .score(passed ? 1.0 : 0.0)
            .passed(passed)
            .summary("Cost: " + cost + " (budget: " + budget + ")")
            .findings(findings)
            .evidence(evidence)
            .build();
    }
}
