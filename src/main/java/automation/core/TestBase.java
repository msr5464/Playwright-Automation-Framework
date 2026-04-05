package automation.core;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.testng.ITestResult;
import org.testng.annotations.*;

@Listeners(automation.core.TestListener.class)
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

    @BeforeSuite
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

        if (environment != null) Config.environment = environment;
        if (browserName != null) Config.browserName = browserName;
        if (projectName != null) Config.projectName = projectName;
        if (country != null) Config.country = country;
        if (appLanguage != null) Config.appLanguage = appLanguage;
        if (groupName != null) Config.groupName = groupName;
        if (branchName != null) Config.branchName = branchName;
        if (resultsDirectory != null) Config.resultsDirectory = resultsDirectory;
        if (debugMode != null) Config.isDebugMode = Boolean.parseBoolean(debugMode);
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
                    // Close browser
                    BrowserHelper.closeBrowser(config);
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
        System.out.println("Test suite completed. Total results: " + testResultObjects.size());
    }
}
