package automation.aiEval.orchestrator;

import automation.aiEval.model.RunSummary;

import java.util.ArrayList;
import java.util.List;

public class CIGate
{
    private final double minPassRate;

    public CIGate(double minPassRate)
    {
        this.minPassRate = minPassRate;
    }

    public void evaluate(RunSummary summary)
    {
        List<String> failures = new ArrayList<>();

        if (summary.getPassRate() < minPassRate)
        {
            failures.add("Pass rate " + format(summary.getPassRate())
                + " below threshold " + format(minPassRate));
        }

        if (!failures.isEmpty())
        {
            throw new CIGateException("CI gate FAILED:\n" + String.join("\n", failures));
        }
    }

    private String format(double value)
    {
        return String.format("%.2f", value);
    }
}
