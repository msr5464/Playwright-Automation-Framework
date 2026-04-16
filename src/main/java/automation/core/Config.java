package automation.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.testng.ITestResult;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Properties;

/**
 * Central configuration class combining static global settings with per-test
 * instance state.
 * Loads properties in order: config.properties -> environment-specific
 * properties -> system.properties.
 */
public class Config {

    // ---------------------------------------------------------------------------
    // Static / Global Settings
    // ---------------------------------------------------------------------------
    // Null means "not yet set by @BeforeSuite" — Config constructor fills from
    // property files only when null.
    public static String browserName = null;
    public static String environment = null;
    public static String country = null;
    public static String appLanguage = null;
    public static String projectName = null;
    public static String groupName = null;
    public static String branchName = null;
    public static String resultsDirectory = null;
    public static Boolean isDebugMode = null;
    public static Boolean isRemoteExecution = null;
    public static Boolean isBrowserStackExecution = null;
    public static String osName = System.getProperty("os.name", "unknown").toLowerCase();

    public static final String mainResourcesPath = "src/main/resources/";
    public static final String testResourcesPath = "src/test/resources/";

    // ---------------------------------------------------------------------------
    // Instance Fields – Playwright objects (per-test)
    // ---------------------------------------------------------------------------
    public Playwright playwright;
    public Browser browser;
    public BrowserContext browserContext;
    public Page page;

    // ---------------------------------------------------------------------------
    // Instance Fields – Mobile / Appium objects (per-test)
    // ---------------------------------------------------------------------------
    public AppiumDriver appiumDriver;
    public AppiumDriverLocalService appiumServer;

    // ---------------------------------------------------------------------------
    // Instance Fields – Test state
    // ---------------------------------------------------------------------------
    public String testcaseName;
    public String testcaseClass;
    public String testStartTime;
    public String testEndTime;
    public String testLog = "";
    public String videoPath = null; // set by BrowserHelper after context closes; read by afterMethod
    public HashMap<String, String> testData = new HashMap<>();
    public TestContext testContext = new TestContext();

    // ---------------------------------------------------------------------------
    // Instance Fields – Control flags
    // ---------------------------------------------------------------------------
    public boolean endExecutionOnFailure = true;
    public boolean enableScreenshot = true;
    public boolean testResult = true;
    public boolean retry = true;
    public java.util.ArrayList<Integer> userId = new java.util.ArrayList<>();
    public boolean isAndroid = false;
    public boolean isIos = false;
    public boolean isProd = false;

    // ---------------------------------------------------------------------------
    // Soft Assert
    // ---------------------------------------------------------------------------
    public SoftAssert softAssert = new SoftAssert();

    // ---------------------------------------------------------------------------
    // Runtime Properties
    // ---------------------------------------------------------------------------
    public Properties runTimeProperties = new Properties();

    // ---------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------

    /**
     * Loads configuration in the following order (later values override earlier
     * ones):
     * 1. parameters/config.properties – flat base settings
     * 2. parameters/{environment}/config.properties – env-subdirectory base
     * settings
     * 3. parameters/{environment}-{country}.properties – flat env+country overrides
     * 4. parameters/{environment}/{environment}-{country}.properties – subdirectory
     * env+country overrides
     * 5. parameters/system.properties – local developer overrides (git-ignored)
     */
    public Config() {
        // 1. Flat base config — loads property file defaults
        loadPropertiesFile("config.properties", "parameters");

        // Static fields set by @BeforeSuite take precedence.
        // If they are still null (beforeSuite hasn't run yet or didn't receive the
        // param),
        // fall back to the property file value, then a hardcoded default.
        // Keys match config.properties and @BeforeSuite @Parameters exactly
        if (environment == null)
            environment = runTimeProperties.getProperty("environment", "staging");
        if (country == null)
            country = runTimeProperties.getProperty("country", "sg");
        if (browserName == null)
            browserName = runTimeProperties.getProperty("browserName", "chromium");
        if (projectName == null)
            projectName = runTimeProperties.getProperty("projectName", "CustomerFrontend");
        if (groupName == null)
            groupName = runTimeProperties.getProperty("groupName", "regression");
        if (branchName == null)
            branchName = runTimeProperties.getProperty("branchName", "main");
        if (appLanguage == null)
            appLanguage = runTimeProperties.getProperty("appLanguage", "en");
        if (isDebugMode == null)
            isDebugMode = Boolean.parseBoolean(runTimeProperties.getProperty("debugMode", "false"));
        if (isRemoteExecution == null)
            isRemoteExecution = Boolean.parseBoolean(runTimeProperties.getProperty("isRemoteExecution", "false"));
        if (resultsDirectory == null)
            resultsDirectory = System.getProperty("user.dir") + File.separator + "test-output";
        if (isBrowserStackExecution == null)
            isBrowserStackExecution = Boolean
                    .parseBoolean(runTimeProperties.getProperty("isBrowserStackExecution", "false"));

        // 2. Environment subdirectory config
        loadPropertiesFile("config.properties", "parameters/" + environment);

        // 3. Flat env+country file (e.g., parameters/staging-sg.properties)
        String envFile = environment + "-" + country + ".properties";
        loadPropertiesFile(envFile, "parameters");

        // 4. Subdirectory env+country file (e.g.,
        // parameters/staging/staging-sg.properties)
        loadPropertiesFile(envFile, "parameters/" + environment);

        // 5. Local developer overrides (git-ignored)
        loadSystemProperties("system.properties", "parameters");

        // Instance-level setting (can vary per environment via property file)
        endExecutionOnFailure = Boolean.parseBoolean(runTimeProperties.getProperty("endExecutionOnFailure", "true"));

        isProd = environment.toLowerCase().contains("prod");
    }

    // ---------------------------------------------------------------------------
    // Property Access
    // ---------------------------------------------------------------------------

    public String getRunTimeProperty(String key) {
        // System properties take highest precedence
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        String value = runTimeProperties.getProperty(key);
        // Fall back to testData map (e.g. values populated during test execution)
        if (value == null && testData != null) {
            value = testData.get(key);
        }
        logCommentForDebugging("Got runtime property: " + key + " = " + value);
        return value;
    }

    public String getRunTimeProperty(String key, String defaultValue) {
        String value = getRunTimeProperty(key);
        return value != null ? value : defaultValue;
    }

    public void putRunTimeProperty(String key, String value) {
        logCommentForDebugging("Setting runtime property: " + key + " = " + value);
        runTimeProperties.setProperty(key, value);
    }

    public void removeRunTimeProperty(String key) {
        runTimeProperties.remove(key);
    }

    /**
     * Replace {$key} placeholders in a string with values from runTimeProperties.
     * Recurses until no {$ tokens remain (handles nested substitutions).
     */
    public String replaceArgumentsWithRunTimeProperties(String input) {
        if (input == null || !input.contains("{$"))
            return input;
        String result = input;
        int safety = 0;
        while (result.contains("{$") && safety++ < 20) {
            int start = result.indexOf("{$");
            int end = result.indexOf("}", start);
            if (end < 0)
                break;
            String key = result.substring(start + 2, end);
            String replacement = getRunTimeProperty(key);
            if (replacement == null)
                replacement = result.substring(start, end + 1); // leave unchanged
            result = result.substring(0, start) + replacement + result.substring(end + 1);
        }
        return result;
    }

    /**
     * Given a list of data values, returns the first non-null value if all non-null
     * elements are equal.
     * Logs a warning and returns null when values disagree.
     */
    public Object getTestDataCollection(Object... dataList) {
        Object reference = null;
        for (Object item : dataList) {
            if (item == null)
                continue;
            if (reference == null) {
                reference = item;
            } else if (!reference.equals(item)) {
                Log.warning(this, "getTestDataCollection: values disagree — '" + reference + "' vs '" + item + "'");
                return null;
            }
        }
        return reference;
    }

    /**
     * Comprehensive end-of-test log. Call from TestBase.afterMethod before closing
     * the browser.
     */
    public void endTest(ITestResult result) {
        testEndTime = DataGenerator.getCurrentDateTime("dd-MM-yyyy HH:mm:ss");
        long durationSeconds = 0;
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(testStartTime, fmt);
            LocalDateTime end = LocalDateTime.parse(testEndTime, fmt);
            durationSeconds = Duration.between(start, end).getSeconds();
        } catch (Exception ignored) {
        }

        long minutes = durationSeconds / 60;
        long seconds = durationSeconds % 60;
        String durationStr = (minutes > 0) ? minutes + "m " + seconds + "s" : seconds + "s";
        System.out.println(
                "Finished test - '" + testcaseName + "' from class - '" + testcaseClass + "' in " + durationStr);
    }

    // ---------------------------------------------------------------------------
    // Property Loading
    // ---------------------------------------------------------------------------

    /**
     * Load system.properties, which may contain local developer overrides.
     */
    public void loadSystemProperties(String filename, String directory) {
        loadPropertiesFile(filename, directory);
    }

    private void loadPropertiesFile(String filename, String directory) {
        try (InputStream inputStream = getInputStream(filename, directory)) {
            if (inputStream != null) {
                runTimeProperties.load(inputStream);
            }
        } catch (Exception e) {
            // File may not exist; this is acceptable for optional config files
            if (isDebugMode) {
                System.out.println("[Config] Could not load " + directory + "/" + filename + ": " + e.getMessage());
            }
        }
    }

    /**
     * Attempt to locate and open a properties file. Searches:
     * 1. {directory}/{filename} relative to project root
     * 2. Classpath resource /{directory}/{filename}
     */
    public InputStream getInputStream(String filename, String directory) {
        // Try filesystem first
        Path filePath = Paths.get(directory, filename);
        if (Files.exists(filePath)) {
            try {
                return new FileInputStream(filePath.toFile());
            } catch (Exception ignored) {
                // Fall through to classpath
            }
        }

        // Try classpath
        String resourcePath = "/" + directory + "/" + filename;
        InputStream classpathStream = getClass().getResourceAsStream(resourcePath);
        if (classpathStream != null) {
            return classpathStream;
        }

        // Try without leading slash
        return getClass().getClassLoader().getResourceAsStream(directory + "/" + filename);
    }

    // ---------------------------------------------------------------------------
    // Logging Helpers — instance methods delegating to Log
    // ---------------------------------------------------------------------------

    public void logComment(String message) {
        Log.comment(this, message);
    }

    public void logPass(String message) {
        Log.pass(this, message);
    }

    public void logFail(String message) {
        Log.fail(this, message);
    }

    public void logWarning(String message) {
        Log.warning(this, message);
    }

    public void logStep(String message) {
        Log.step(this, message);
    }

    public void logAction(String message) {
        Log.action(this, message);
    }

    /** Logs only when debug mode is enabled. */
    public void logCommentForDebugging(String message) {
        if (isDebugMode)
            Log.comment(this, message);
    }

    /** Logs with a custom HTML color. */
    public void logColorfulComment(String message, String color) {
        Log.comment(this, message, color);
    }

    /** Marks test as non-retryable, forces hard stop, and logs the failure. */
    public void logFailToEndExecution(String message) {
        retry = false;
        endExecutionOnFailure = true;
        Log.fail(this, message);
    }

    /** Logs a failure message + cleaned exception stack trace and hard-stops. */
    public void logExceptionAndFail(String message, Throwable e) {
        Log.failure(this, message + ": " + e.getMessage(), e);
    }

    /** Logs as a warning with exception details (does not stop test). */
    public void logException(String message, Throwable e) {
        Log.warning(this, message + ": " + e.getMessage(), e);
    }
}
