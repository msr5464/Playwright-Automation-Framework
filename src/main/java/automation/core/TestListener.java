package automation.core;

import org.testng.*;

import automation.core.Config;
import automation.core.BrowserHelper;
import automation.core.Log;

public class TestListener implements ITestListener, IRetryAnalyzer
{

    private int retryCount = 0;
    private static final int MAX_RETRY = 2;

    @Override
    public boolean retry(ITestResult result)
    {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof Config config)
        {
            if (config.retry && retryCount < MAX_RETRY)
            {
                retryCount++;
                Log.comment(config, "Retrying test '" + config.testcaseName + "' - Attempt " + retryCount + "/" + MAX_RETRY);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTestStart(ITestResult result)
    {
        // Log test start
    }

    @Override
    public void onTestSuccess(ITestResult result)
    {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof Config config)
        {
            config.testResult = true;
            config.testEndTime = automation.core.DataGenerator.getCurrentDateTime("dd-MM-yyyy HH:mm:ss");
            Log.pass(config, "Test PASSED: " + config.testcaseName);
        }
    }

    @Override
    public void onTestFailure(ITestResult result)
    {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof Config config)
        {
            config.testResult = false;
            config.testEndTime = automation.core.DataGenerator.getCurrentDateTime("dd-MM-yyyy HH:mm:ss");
            // Take screenshot on failure
            if (config.enableScreenshot && config.page != null)
            {
                String screenshotLink = BrowserHelper.takeScreenshot(config);
                Log.fail(config, "Test FAILED: " + config.testcaseName + (screenshotLink != null ? " " + screenshotLink : ""));
            }
            else
            {
                Log.fail(config, "Test FAILED: " + config.testcaseName);
            }
            // Log failure details
            if (result.getThrowable() != null)
            {
                Log.fail(config, "Failure: " + result.getThrowable().getMessage());
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result)
    {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof Config config)
        {
            Log.warning(config, "Test SKIPPED: " + config.testcaseName);
        }
    }

    @Override
    public void onStart(ITestContext context)
    {
        System.out.println("Test Suite Started: " + context.getName());
    }

    @Override
    public void onFinish(ITestContext context)
    {
        // Remove retried tests from failed list (keep only the last retry result)
        context.getFailedTests().getAllResults().removeIf(result ->
        {
            IRetryAnalyzer retry = result.getMethod().getRetryAnalyzer(result);
            return retry != null && context.getFailedTests().getResults(result.getMethod()).size() > 1;
        });
        System.out.println("Test Suite Finished: " + context.getName());
    }
}
