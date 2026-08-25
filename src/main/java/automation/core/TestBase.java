package automation.core;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.*;

// Registered here, not only in testng.xml: surefire ignores <suiteXmlFiles>
// when a run is targeted with -Dtest=Class#method, so a listener declared
// only in the suite file silently does not run. JsonTestReporter writes the
// machine-readable report.json the QA agent network reads, and it has to
// exist for a single-test run too. TestNG de-duplicates listeners, so the
// testng.xml entries remain harmless.
@Listeners({automation.core.TestListener.class, automation.core.JsonTestReporter.class})
public class TestBase
{

    protected static final long DEFAULT_TEST_TIMEOUT = 600000;
    protected static final int THREAD_COUNT = 2;

    // Test group constants
    protected static final String GROUP_REGRESSION = "regression";
    protected static final String GROUP_API = "apiCases";
    protected static final String GROUP_WEB = "webCases";
    protected static final String GROUP_ANDROID = "androidCases";
    protected static final String GROUP_IOS = "iosCases";
    protected static final String GROUP_PROD_SANITY = "prodSanity";
    protected static final String GROUP_DEMO_SANITY = "demoSanity";
    protected static final String GROUP_SMOKE = "smokeTest";
    protected static final String GROUP_CRITICAL = "criticalFlows";

    // Thread-safe config and context storage
    protected static ThreadLocal<Config[]> threadLocalConfig = new ThreadLocal<>();
    protected static ThreadLocal<TestContext> threadLocalContext = new ThreadLocal<>();

    // Test result collection for reporting
    public static List<TestRailHelper.TestResultObject> testResultObjects =
        java.util.Collections.synchronizedList(new ArrayList<>());

    /**
     * Resolves a config value in priority order:
     * 1. TestNG suite XML parameter (passed via @Parameters)
     * 2. -D system property
     * 3. Current static field value (default / previously set)
     */
    private static String resolve(String suiteParam, String sysPropKey, String currentValue)
    {
        if (suiteParam != null && !suiteParam.isEmpty()) return suiteParam;
        String sysProp = System.getProperty(sysPropKey);
        if (sysProp != null && !sysProp.isEmpty()) return sysProp;
        return currentValue;
    }

    /**
     * Get TestContext for current thread (parallel-safe shortcut)
     */
    protected TestContext ctx()
    {
        return threadLocalContext.get();
    }

    /**
     * Get TestContext for specific config (multi-config tests)
     */
    protected TestContext ctx(Config config)
    {
        return config.testContext;
    }

    /**
     * Allocate a user from the pool, store in TestContext, and register for auto-cleanup.
     *
     * Before:
     *   User user = UserManagement.getFreeUser(config.testcaseName,
     *       q -> q.withUserType(UserType.Admin).withFeature(Feature.CARD).withCountry(Country.SG));
     *   ctx().addUser("admin", user);
     *   config.userId.add(user.getId());
     *
     * After:
     *   User user = allocateUser(config, "admin", q -> q.withUserType(Admin).withFeature(CARD).withCountry(SG));
     */
    protected User allocateUser(Config config, String label,
                                java.util.function.Function<UserManagement.UserQueryBuilder,
                                    UserManagement.UserQueryBuilder> queryFn)
    {
    	UserManagement.initialize(Config.environment, Enums.Country.valueOf(Config.country.toUpperCase()));
        User user = UserManagement.getFreeUser(config, config.testcaseName, queryFn);
        ctx(config).addUser(label, user);
        config.userId.add(user.getId());
        Log.step(config, "Allocated user '" + label + "': " + user.getUsername());
        return user;
    }

    /** Convenience: label derived from UserType name (e.g. UserType.Admin → "admin"). */
    protected User allocateUser(Config config, Enums.UserType userType, Enums.Feature feature, Enums.Country country)
    {
        return allocateUser(config, userType.name().toLowerCase(), q -> q.withUserType(userType).withFeature(feature).withCountry(country));
    }

    /**
     * Log a step in the test
     */
    public void logStep(Config config, String message)
    {
        Log.step(config, message);
    }

    /**
     * Get upload file destination path (for mobile tests)
     */
    protected Path getUploadFileDestinationPath(String fileName)
    {
        String[] imageExtensions = {"jpg", "png", "gif", "bmp", "jpeg"};
        String fileExtension = FilenameUtils.getExtension(fileName).toLowerCase();
        boolean isImageFile = Arrays.asList(imageExtensions).contains(fileExtension);
        String destinationFolder = isImageFile ? "Pictures" : "Download";
        return Path.of(File.separator + "sdcard" + File.separator + destinationFolder + File.separator + fileName);
    }

    // ========== DATA PROVIDERS ==========

    @DataProvider(name = "getConfig")
    public Object[][] getConfig(Method method)
    {
        Config config = new Config();
        config.testcaseName = method.getName();
        config.testcaseClass = method.getDeclaringClass().getName();
        config.testStartTime = DataGenerator.getCurrentDateTime("dd-MM-yyyy HH:mm:ss");
        System.out.println("Running test - '" + config.testcaseName + "' from class - '" + config.testcaseClass + "'...");
        threadLocalConfig.set(new Config[]{config});
        threadLocalContext.set(config.testContext);
        return new Object[][]{{config}};
    }

    @DataProvider(name = "getTwoConfigs")
    public Object[][] getTwoConfigs(Method method)
    {
        Config config = new Config();
        Config secondaryConfig = new Config();
        config.testcaseName = method.getName();
        config.testcaseClass = method.getDeclaringClass().getName();
        secondaryConfig.testcaseName = method.getName();
        secondaryConfig.testcaseClass = method.getDeclaringClass().getName();
        config.testStartTime = DataGenerator.getCurrentDateTime("dd-MM-yyyy HH:mm:ss");
        secondaryConfig.testStartTime = DataGenerator.getCurrentDateTime("dd-MM-yyyy HH:mm:ss");
        System.out.println("Running test - '" + config.testcaseName + "' from class - '" + config.testcaseClass + "'...");
        threadLocalConfig.set(new Config[]{config, secondaryConfig});
        threadLocalContext.set(config.testContext);
        return new Object[][]{{config, secondaryConfig}};
    }

    @DataProvider(name = "getMultipleConfigs")
    public Object[][] getMultipleConfigs(Method method)
    {
        int configCount = 3; // Default 3 configs
        Config[] configs = new Config[configCount];
        for (int i = 0; i < configCount; i++)
        {
            configs[i] = new Config();
            configs[i].testcaseName = method.getName();
            configs[i].testcaseClass = method.getDeclaringClass().getName();
            configs[i].testStartTime = DataGenerator.getCurrentDateTime("dd-MM-yyyy HH:mm:ss");
        }
        threadLocalConfig.set(configs);
        threadLocalContext.set(configs[0].testContext);
        return new Object[][]{configs};
    }

    // ========== LIFECYCLE HOOKS ==========

    @BeforeSuite(alwaysRun = true)
    @Parameters({"environment", "browserName", "projectName", "country", "appLanguage",
                 "groupName", "branchName", "resultsDirectory", "debugMode"})
    public void beforeSuite(
            @Optional String environment,
            @Optional String browserName,
            @Optional String projectName,
            @Optional String country,
            @Optional String appLanguage,
            @Optional String groupName,
            @Optional String branchName,
            @Optional String resultsDirectory,
            @Optional String debugMode)
    {

        // If statics are still null (no GenerateTestngXmlAndRun, direct -Dtest= run),
        // bootstrap from property files so they are never null before resolve() runs.
        if (Config.environment == null) new Config();

        // Priority: suite XML param → -D system property → already-set static (from GenerateTestngXmlAndRun or property file bootstrap)
        Config.environment      = resolve(environment,      "environment",      Config.environment);
        Config.browserName      = resolve(browserName,        "browserName",      Config.browserName);
        Config.projectName      = resolve(projectName,      "projectName",      Config.projectName);
        Config.country          = resolve(country,          "country",          Config.country);
        Config.appLanguage      = resolve(appLanguage,      "appLanguage",      Config.appLanguage);
        Config.groupName        = resolve(groupName,        "groupName",        Config.groupName);
        Config.branchName       = resolve(branchName,       "branchName",       Config.branchName);
        Config.resultsDirectory = resolve(resultsDirectory, "resultsDirectory", Config.resultsDirectory);
        Config.isDebugMode             = Boolean.parseBoolean(resolve(debugMode,            "debugMode",              String.valueOf(Config.isDebugMode)));
        Config.isRemoteExecution       = Boolean.parseBoolean(resolve(null,                 "isRemoteExecution",      String.valueOf(Config.isRemoteExecution)));
        Config.isBrowserStackExecution = Boolean.parseBoolean(resolve(null,                 "isBrowserStackExecution",String.valueOf(Config.isBrowserStackExecution)));

        // Create per-project results table once per suite (only when remote execution is enabled)
        if (Config.isRemoteExecution)
        {
            createResultsTableIfNotExists();
        }
    }

    private void createResultsTableIfNotExists()
    {
        try
        {
            Config config = new Config();
            String tableName = "results_" + Config.projectName.toLowerCase();
            String createTableQuery = "CREATE TABLE IF NOT EXISTS `" + tableName + "` ("
                + "`id` int NOT NULL AUTO_INCREMENT,"
                + "`createdAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP,"
                + "`environment` varchar(25) DEFAULT NULL,"
                + "`groupName` varchar(25) DEFAULT NULL,"
                + "`testrailSuiteId` varchar(10) DEFAULT NULL,"
                + "`testrailCaseId` varchar(100) DEFAULT NULL,"
                + "`testStatus` varchar(10) DEFAULT NULL,"
                + "`failureReason` text,"
                + "`platform` varchar(20) DEFAULT NULL,"
                + "`automatedBy` varchar(50) DEFAULT NULL,"
                + "`maintainedBy` varchar(50) DEFAULT NULL,"
                + "`testcaseName` varchar(250) NOT NULL,"
                + "`buildTag` varchar(50) DEFAULT NULL,"
                + "`testrailUploadRequired` tinyint NOT NULL,"
                + "`uploadedToTestrail` tinyint NOT NULL,"
                + "`knownFailure` varchar(50) DEFAULT NULL,"
                + "PRIMARY KEY (`id`))";
            DatabaseHelper.executeQuery(config, createTableQuery, automation.core.Enums.QueryType.create, automation.core.Enums.DatabaseName.Automation);
            Log.info("Results table ready: " + tableName);
        }
        catch (Exception e)
        {
            Log.error("Failed to create results table: " + e.getMessage());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result)
    {
        Config[] configs = threadLocalConfig.get();
        if (configs != null)
        {
            for (Config config : configs)
            {
                try
                {
                    // Release allocated users back to pool
                    if (!config.userId.isEmpty())
                    {
                        UserManagement.releaseUsers(config, config.userId);
                    }
                    // Close browser (video path is stored in config.videoPath after this)
                    BrowserHelper.closeBrowser(config);

                    // Log video link with explicit ITestResult so ReportNG associates
                    // it with the correct test — Reporter.log() alone is unreliable in @AfterMethod
                    if (config.videoPath != null)
                    {
                        String videoHtml = "<a href='" + config.videoPath + "' target='_blank' style='color:#2563EB;'>&#127909; View Recording</a>";
                        ITestResult previous = Reporter.getCurrentTestResult();
                        Reporter.setCurrentTestResult(result);
                        Log.comment(config, videoHtml);
                        Reporter.setCurrentTestResult(previous);
                    }
                }
                catch (Exception e)
                {
                    Log.error("Error in afterMethod cleanup: " + e.getMessage());
                }
            }
        }
        threadLocalConfig.remove();
        threadLocalContext.remove();
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite()
    {
        // Upload results to TestRail, send Slack/Email notifications
    }
}
