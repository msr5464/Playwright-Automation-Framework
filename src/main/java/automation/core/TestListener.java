package automation.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import automation.core.Enums.DatabaseName;
import automation.core.Enums.QueryType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestListener implements ITestListener, IInvokedMethodListener, IAnnotationTransformer, IRetryAnalyzer
{

    private int retryCount = 0;
    private static final int MAX_RETRY = 2;

    // ========== IAnnotationTransformer — auto-wire retry on every @Test ==========

    @Override
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod)
    {
        annotation.setRetryAnalyzer(TestListener.class);
    }

    // ========== IRetryAnalyzer ==========

    @Override
    public boolean retry(ITestResult result)
    {
        Config[] configs = TestBase.threadLocalConfig.get();
        if (configs != null)
        {
            for (Config config : configs)
            {
                if (config != null && config.retry && retryCount < MAX_RETRY)
                {
                    retryCount++;
                    Log.comment(config, "Retrying test '" + config.testcaseName + "' - Attempt " + retryCount + "/" + MAX_RETRY);
                    return true;
                }
            }
        }
        return false;
    }

    // ========== IInvokedMethodListener ==========

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult)
    {
        if (method.isTestMethod())
        {
            Config[] configs = TestBase.threadLocalConfig.get();
            if (configs != null)
            {
                for (Config config : configs)
                {
                    if (config != null)
                    {
                        String description = testResult.getMethod().getDescription();
                        String label = (description != null && !description.isEmpty()) ? description : testResult.getMethod().getMethodName();
                        config.logColorfulComment(
                            "<b style='font-size:13px;padding:2px 6px;background-color:#2196F3;border-radius:3px;'>"
                            + "Description: " + label + "</b>", "white");
                    }
                }
            }
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult)
    {
        // Clean stack trace as early as possible
        if (testResult.getThrowable() != null)
        {
            testResult.getThrowable().setStackTrace(
                Log.getCleanedStackTraceElements(testResult.getThrowable().getStackTrace()));
        }

        // Flush soft assertions: if test "passed" but had soft failures, mark it FAILED
        if (method.isTestMethod() &&
            (testResult.getStatus() == ITestResult.SUCCESS || testResult.getStatus() == ITestResult.FAILURE))
        {
            Config[] configs = TestBase.threadLocalConfig.get();
            if (configs != null)
            {
                for (Config config : configs)
                {
                    if (config != null && testResult.getStatus() == ITestResult.SUCCESS)
                    {
                        try
                        {
                            config.softAssert.assertAll();
                        }
                        catch (AssertionError e)
                        {
                            e.setStackTrace(Log.getCleanedStackTraceElements(e.getStackTrace()));
                            testResult.setStatus(ITestResult.FAILURE);
                            testResult.setThrowable(e);
                        }
                    }
                }
            }
        }
    }

    // ========== ITestListener ==========

    @Override
    public void onTestStart(ITestResult result)
    {
        // handled in beforeInvocation
    }

    @Override
    public void onTestSuccess(ITestResult result)
    {
        Config[] configs = TestBase.threadLocalConfig.get();
        if (configs != null)
        {
            for (Config config : configs)
            {
                if (config != null)
                {
                    config.testResult = true;
                    config.endTest(result);
                    insertTestResultToDb(config, result, "PASSED", null);
                }
            }
        }
    }

    @Override
    public void onTestFailure(ITestResult result)
    {
        Config[] configs = TestBase.threadLocalConfig.get();

        // Log end-of-test marker — stack trace is already shown in the ReportNG exception section
        if (configs != null)
        {
            for (Config config : configs)
            {
                if (config != null)
                {
                    config.logComment("------------END OF EXECUTION------------");
                }
            }
        }

        // Mark failure + screenshot + endTest
        if (configs != null)
        {
            for (Config config : configs)
            {
                if (config != null)
                {
                    config.testResult = false;
                    if (config.enableScreenshot && config.page != null)
                    {
                        BrowserHelper.takeScreenshot(config);
                    }
                    // Capture the DOM too, not just a picture of it. This is what
                    // lets an automated fixer see why a locator stopped matching,
                    // without having to replay the flow to get back here.
                    if (config.page != null)
                    {
                        BrowserHelper.captureDomSnapshot(config);
                    }
                    // repairMode: park the browser on the failing page and publish
                    // how to reach it, so a fixing agent can attach to the live
                    // session rather than work from a static capture.
                    if (config.cdpPort > 0)
                    {
                        openRepairSession(config, result);
                    }
                    config.endTest(result);
                    String failureReason = result.getThrowable() != null
                        ? result.getThrowable().getMessage() : "Unknown failure";
                    insertTestResultToDb(config, result, "FAILED", failureReason);
                }
            }
        }
    }

    /**
     * Publish a live repair session for the test that just failed.
     *
     * Writes {resultsDirectory}/.repair-session.json describing the parked browser
     * and marks the config so afterMethod does not tear it down. The QA agent
     * network reads this file, attaches Playwright MCP to the CDP endpoint, and
     * inspects the real failing page — where it can count how many elements a
     * candidate selector matches and try a corrected locator before any Java is
     * edited. A static capture cannot do either.
     */
    private void openRepairSession(Config config, ITestResult result)
    {
        try
        {
            config.keepBrowserOpen = true;
            String url = config.failureUrl == null ? "" : config.failureUrl;
            String testName = result.getTestClass().getName() + "." + result.getName();
            String json = "{\n"
                + "  \"cdpEndpoint\": \"http://localhost:" + config.cdpPort + "\",\n"
                // The agent reaps this browser by pid once it has finished
                // inspecting: nothing else ever will, because the process is
                // deliberately detached from the JVM.
                + "  \"browserPid\": " + config.repairBrowserPid + ",\n"
                + "  \"test\": \"" + testName + "\",\n"
                + "  \"url\": \"" + url + "\",\n"
                + "  \"domSnapshot\": \"" + (config.domSnapshotPath == null ? "" : config.domSnapshotPath.replace("\\", "/")) + "\",\n"
                + "  \"openedAt\": \"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()) + "\"\n"
                + "}\n";
            File sessionFile = new File(Config.resultsDirectory, ".repair-session.json");
            try (java.io.FileWriter writer = new java.io.FileWriter(sessionFile))
            {
                writer.write(json);
            }
            Log.comment(config, "Repair session open: " + sessionFile.getAbsolutePath());
            System.out.println("[RepairMode] Browser parked on the failing page. "
                + "CDP: http://localhost:" + config.cdpPort);
            System.out.println("[RepairMode] Run: make run AGENT=test-healing-agent");
        }
        catch (Exception e)
        {
            Log.warning(config, "Could not open repair session: " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result)
    {
        Config[] configs = TestBase.threadLocalConfig.get();
        if (configs != null)
        {
            for (Config config : configs)
            {
                if (config != null)
                {
                    Log.warning(config, "Test SKIPPED: " + config.testcaseName);
                }
            }
        }
    }

    // ========== Per-testcase DB insertion ==========

    private void insertTestResultToDb(Config config, ITestResult result, String testStatus, String rawFailureReason)
    {
        if (!Config.isRemoteExecution) return;
        try
        {
            // Read @TestVariables metadata from the test method
            Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
            String automatedBy = "Unassigned";
            String maintainedBy = "Unassigned";
            String testrailSuiteId = "";
            String testrailCaseIds = "";
            String testrailUploadRequired = "0";
            if (testMethod != null && testMethod.isAnnotationPresent(TestVariables.class))
            {
                TestVariables tv = testMethod.getAnnotation(TestVariables.class);
                automatedBy = tv.automatedBy().name();
                maintainedBy = tv.maintainedBy().name();
                if (!tv.testrailData().isEmpty())
                {
                    String[] parts = tv.testrailData().split(":");
                    if (parts.length >= 2) { testrailSuiteId = parts[0]; testrailCaseIds = parts[1]; }
                    testrailUploadRequired = "1";
                }
            }

            // buildTag = last path segment of resultsDirectory (matches Thanos pattern)
            String[] pathParts = Config.resultsDirectory.split("[/\\\\]");
            String buildTag = pathParts[pathParts.length - 1];

            // platform derived from browser setting
            String browserName = config.getRunTimeProperty("browserName", "chromium");
            String platform = switch (browserName.toLowerCase())
            {
                case "api"     -> "API";
                case "android" -> "Android";
                case "ios"     -> "iOS";
                default        -> "Web";
            };

            // Enrich failure reason with testcase name + result link (mirrors Thanos)
            String resultLink = Config.resultsDirectory + File.separator + "html" + File.separator + "index.html";
            String enrichedFailure = (rawFailureReason != null ? "Failure Reason: " + rawFailureReason + "\n" : "")
                + "Testcase Name: " + config.testcaseName + "\n"
                + "Results Url: " + resultLink;
            String sanitizedFailure = enrichedFailure.replace("'", "\"").replace("\r", "").trim();

            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            String tableName = "results_" + Config.projectName.toLowerCase();

            // Handle comma-separated testrail case IDs (same as Thanos)
            String[] caseIds = testrailCaseIds.isEmpty() ? new String[]{""} : testrailCaseIds.split(",");
            for (String caseId : caseIds)
            {
                String insertQuery = "INSERT INTO `" + tableName + "` "
                    + "(`createdAt`,`environment`,`groupName`,`testrailSuiteId`,`testrailCaseId`,`testStatus`,"
                    + "`failureReason`,`platform`,`automatedBy`,`maintainedBy`,`testcaseName`,`buildTag`,"
                    + "`testrailUploadRequired`,`uploadedToTestrail`,`knownFailure`) VALUES ("
                    + "'" + createdAt + "',"
                    + "'" + Config.environment.toUpperCase() + "',"
                    + "'" + Config.groupName + "',"
                    + "'" + testrailSuiteId + "',"
                    + "'" + caseId.trim() + "',"
                    + "'" + testStatus + "',"
                    + "'" + sanitizedFailure + "',"
                    + "'" + platform + "',"
                    + "'" + automatedBy + "',"
                    + "'" + maintainedBy + "',"
                    + "'" + config.testcaseName + "',"
                    + "'" + buildTag + "',"
                    + "'" + testrailUploadRequired + "',"
                    + "'0',"
                    + "NULL)";

                DatabaseHelper.executeQuery(config, insertQuery, QueryType.update, DatabaseName.Automation);
            }
        }
        catch (Exception e)
        {
            Log.error("Failed to insert test result to DB for '" + config.testcaseName + "': " + e.getMessage());
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
        // Remove stale failed/skipped results when a later retry passed or succeeded
        try
        {
            for (ITestNGMethod method : context.getAllTestMethods())
            {
                IRetryAnalyzer retry = method.getRetryAnalyzer(null);
                if (retry != null)
                {
                    if (!context.getFailedTests().getResults(method).isEmpty()
                            && !context.getPassedTests().getResults(method).isEmpty())
                    {
                        context.getFailedTests().removeResult(method);
                    }
                    if (!context.getSkippedTests().getResults(method).isEmpty()
                            && !context.getFailedTests().getResults(method).isEmpty())
                    {
                        context.getSkippedTests().removeResult(method);
                    }
                    if (!context.getSkippedTests().getResults(method).isEmpty()
                            && !context.getPassedTests().getResults(method).isEmpty())
                    {
                        context.getSkippedTests().removeResult(method);
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception in onFinish: " + e.getMessage());
        }
        System.out.println("Test Suite Finished: " + context.getName());
    }
}
