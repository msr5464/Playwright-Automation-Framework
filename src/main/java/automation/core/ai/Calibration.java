package automation.core.ai;

import automation.core.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * How well automated verdicts agree with your labels — the check every grader (code or LLM judge) must pass
 * before its numbers are trusted.
 *
 *   TPR   — of the cases you passed, the share the evals also passed
 *   TNR   — of the cases you failed, the share the evals also failed
 *   kappa — agreement corrected for chance: 1 is perfect, 0 is no better than guessing
 *
 * Run on a labelled sheet:
 *   mvn -q compile exec:java@calibration -Dexec.args="path/to/labels.csv"
 */
public record Calibration(int bothPass, int humanPassEvalFail, int humanFailEvalPass, int bothFail)
{
    public static Calibration of(List<LabelSheet.LabelledCase> labelledCases)
    {
        int bothPass = 0;
        int humanPassEvalFail = 0;
        int humanFailEvalPass = 0;
        int bothFail = 0;
        for (LabelSheet.LabelledCase labelledCase : labelledCases)
        {
            if (labelledCase.humanPass() && labelledCase.evalPass()) bothPass++;
            else if (labelledCase.humanPass()) humanPassEvalFail++;
            else if (labelledCase.evalPass()) humanFailEvalPass++;
            else bothFail++;
        }
        return new Calibration(bothPass, humanPassEvalFail, humanFailEvalPass, bothFail);
    }

    public double truePositiveRate()
    {
        return share(bothPass, bothPass + humanPassEvalFail);
    }

    public double trueNegativeRate()
    {
        return share(bothFail, bothFail + humanFailEvalPass);
    }

    public double kappa()
    {
        double total = bothPass + humanPassEvalFail + humanFailEvalPass + bothFail;
        double observed = (bothPass + bothFail) / total;
        double expected = ((bothPass + humanPassEvalFail) * (bothPass + humanFailEvalPass)
            + (humanFailEvalPass + bothFail) * (humanPassEvalFail + bothFail)) / (total * total);
        // Both graders gave every case the same single verdict: agreement is total but chance can't be separated out
        return expected == 1.0 ? observed : (observed - expected) / (1.0 - expected);
    }

    // NaN when there is nothing to measure (e.g. TPR with no human passes)
    private static double share(int part, int whole)
    {
        return whole == 0 ? Double.NaN : (double) part / whole;
    }

    public static void main(String[] args) throws IOException
    {
        List<LabelSheet.LabelledCase> labelledCases = LabelSheet.read(new File(args[0]));
        Calibration calibration = of(labelledCases);
        Log.info(labelledCases.size() + " labelled cases");
        Log.info(String.format("TPR   %.2f  (you passed %d; evals agreed on %d)",
            calibration.truePositiveRate(), calibration.bothPass() + calibration.humanPassEvalFail(), calibration.bothPass()));
        Log.info(String.format("TNR   %.2f  (you failed %d; evals agreed on %d)",
            calibration.trueNegativeRate(), calibration.bothFail() + calibration.humanFailEvalPass(), calibration.bothFail()));
        Log.info(String.format("kappa %.2f", calibration.kappa()));
    }
}
