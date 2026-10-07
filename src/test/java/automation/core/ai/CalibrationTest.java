package automation.core.ai;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestBase;
import automation.core.TestVariables;
import org.testng.annotations.Test;

import java.util.List;

public class CalibrationTest extends TestBase
{
    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void of_countsEachCombinationOfVerdicts(Config config)
    {
        Calibration calibration = Calibration.of(List.of(
            new LabelSheet.LabelledCase("A", true, true),
            new LabelSheet.LabelledCase("B", true, false),
            new LabelSheet.LabelledCase("C", false, true),
            new LabelSheet.LabelledCase("D", false, false)));

        AssertHelper.assertEquals(config, calibration.bothPass(), 1, "Cases both passed");
        AssertHelper.assertEquals(config, calibration.humanPassEvalFail(), 1, "Cases only you passed");
        AssertHelper.assertEquals(config, calibration.humanFailEvalPass(), 1, "Cases only the evals passed");
        AssertHelper.assertEquals(config, calibration.bothFail(), 1, "Cases both failed");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void mixedVerdicts_tprTnrAndKappa(Config config)
    {
        Calibration calibration = new Calibration(40, 10, 5, 45);

        AssertHelper.assertEquals(config, calibration.truePositiveRate(), 0.8, "TPR (40 of your 50 passes)");
        AssertHelper.assertEquals(config, calibration.trueNegativeRate(), 0.9, "TNR (45 of your 50 fails)");
        AssertHelper.assertEquals(config, calibration.kappa(), 0.7, "Kappa");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void alwaysPassGrader_agrees90PercentButTnrAndKappaAreZero(Config config)
    {
        // You failed 10 of 100 outputs; a grader that passes everything still "agrees" 90% of the time
        Calibration calibration = new Calibration(90, 0, 10, 0);

        AssertHelper.assertEquals(config, calibration.trueNegativeRate(), 0.0, "TNR");
        AssertHelper.assertEquals(config, calibration.kappa(), 0.0, "Kappa");
    }
}
