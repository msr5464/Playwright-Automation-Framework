package automation.core.ai;

import automation.core.Config;
import automation.core.DataGenerator;
import automation.core.TestBase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Base class for eval tests: runs a test method once per dataset case, and writes the run report
 * ({@link EvalRunListener}) after the suite.
 *
 *   @Test(dataProvider = "evalCases", groups = {GROUP_REGRESSION, GROUP_API})
 *   @EvalDataset(module = "aiteststudio", name = "talk-to-tests")
 *   public void answersDatasetQuestions(Config config, EvalCase evalCase) { ... }
 */
@Listeners(EvalRunListener.class)
public class EvalTestBase extends TestBase
{
    @DataProvider(name = "evalCases")
    public Object[][] evalCases(Method method) throws IOException
    {
        EvalDataset dataset = method.getAnnotation(EvalDataset.class);
        if (dataset == null)
        {
            throw new IllegalStateException(method.getName() + " uses dataProvider \"evalCases\" but has no @EvalDataset");
        }
        File directory = new File(System.getProperty("user.dir"),
            "src/test/resources/" + dataset.module() + "/evalCases/" + dataset.name());
        List<EvalCase> cases = new EvalCaseLoader().loadFromDirectory(directory);
        if (cases.isEmpty())
        {
            throw new IllegalStateException("No eval cases found in " + directory);
        }

        Object[][] rows = new Object[cases.size()][];
        for (int i = 0; i < cases.size(); i++)
        {
            Config config = new Config();
            config.testcaseName = method.getName() + "[" + cases.get(i).getCaseId() + "]";
            config.testcaseClass = method.getDeclaringClass().getName();
            config.testStartTime = DataGenerator.getCurrentDateTime("dd-MM-yyyy HH:mm:ss");
            // A failed case must stay failed: retrying until it passes would report pass@3 as the pass rate
            config.retry = false;
            rows[i] = new Object[]{config, cases.get(i)};
        }
        return rows;
    }

    // getConfig binds its one Config when the data provider runs. With one row per case, bind each case's own
    // Config right before its invocation, so soft assertions, retries and cleanup act on that case.
    @BeforeMethod(alwaysRun = true)
    public void bindCaseConfig(Object[] testParameters)
    {
        if (testParameters.length > 0 && testParameters[0] instanceof Config config)
        {
            threadLocalConfig.set(new Config[]{config});
            threadLocalContext.set(config.testContext);
        }
    }
}
