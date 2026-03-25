package automation.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.testng.asserts.SoftAssert;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Central configuration class combining static global settings with per-test instance state.
 * Loads properties in order: config.properties -> environment-specific properties -> system.properties.
 */
public class Config
{

    // ---------------------------------------------------------------------------
    // Static / Global Settings
    // ---------------------------------------------------------------------------
    public static String browserName = "chromium";
    public static String environment = "qa-1";
    public static String country = "sg";
    public static String appLanguage = "en";
    public static String projectName = "CustomerFrontend";
    public static String groupName = "";
    public static String branchName = "";
    public static String resultsDirectory = "test-results";
    public static boolean isDebugMode = false;
    public static boolean isRemoteExecution = false;
    public static boolean isBrowserStackExecution = false;
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
     * Loads configuration in the following order (later values override earlier ones):
     * 1. parameters/config.properties                        – flat base settings
     * 2. parameters/{environment}/config.properties          – env-subdirectory base settings
     * 3. parameters/{environment}-{country}.properties       – flat env+country overrides
     * 4. parameters/{environment}/{environment}-{country}.properties – subdirectory env+country overrides
     * 5. parameters/system.properties                        – local developer overrides (git-ignored)
     */
    public Config()
    {
        // 1. Try flat base config first
        loadPropertiesFile("config.properties", "parameters");

        // 2. Try environment subdirectory config (overrides flat)
        loadPropertiesFile("config.properties", "parameters/" + environment);

        // Apply static fields from loaded properties
        browserName = runTimeProperties.getProperty("browser", browserName);
        environment = runTimeProperties.getProperty("environment", environment);
        country = runTimeProperties.getProperty("country", country);
        appLanguage = runTimeProperties.getProperty("appLanguage", appLanguage);
        projectName = runTimeProperties.getProperty("projectName", projectName);
        groupName = runTimeProperties.getProperty("groupName", groupName);
        branchName = runTimeProperties.getProperty("branchName", branchName);
        resultsDirectory = runTimeProperties.getProperty("resultsDirectory", resultsDirectory);
        isDebugMode = Boolean.parseBoolean(runTimeProperties.getProperty("debugMode", "false"));
        isRemoteExecution = Boolean.parseBoolean(runTimeProperties.getProperty("isRemoteExecution", "false"));
        isBrowserStackExecution = Boolean.parseBoolean(runTimeProperties.getProperty("isBrowserStackExecution", "false"));
        // mobilePlatform is resolved per-test when AppiumDriverManager is invoked

        // 3. Flat env+country file (e.g., parameters/demo-sg.properties)
        String envFile = environment + "-" + country + ".properties";
        loadPropertiesFile(envFile, "parameters");

        // 4. Subdirectory env+country file (e.g., parameters/demo/demo-sg.properties)
        loadPropertiesFile(envFile, "parameters/" + environment);

        // 5. Load system.properties (local overrides, git-ignored)
        loadSystemProperties("system.properties", "parameters");

        // Determine if production environment
        isProd = environment.toLowerCase().contains("prod");
    }

    // ---------------------------------------------------------------------------
    // Property Access
    // ---------------------------------------------------------------------------

    public String getRunTimeProperty(String key)
    {
        // System properties take highest precedence
        String systemValue = System.getProperty(key);
        if (systemValue != null)
        {
            return systemValue;
        }
        return runTimeProperties.getProperty(key);
    }

    public String getRunTimeProperty(String key, String defaultValue)
    {
        String value = getRunTimeProperty(key);
        return value != null ? value : defaultValue;
    }

    public void putRunTimeProperty(String key, String value)
    {
        runTimeProperties.setProperty(key, value);
    }

    // ---------------------------------------------------------------------------
    // Property Loading
    // ---------------------------------------------------------------------------

    /**
     * Load system.properties, which may contain local developer overrides.
     */
    public void loadSystemProperties(String filename, String directory)
    {
        loadPropertiesFile(filename, directory);
    }

    private void loadPropertiesFile(String filename, String directory)
    {
        try (InputStream inputStream = getInputStream(filename, directory))
        {
            if (inputStream != null)
            {
                runTimeProperties.load(inputStream);
            }
        }
        catch (Exception e)
        {
            // File may not exist; this is acceptable for optional config files
            if (isDebugMode)
            {
                System.out.println("[Config] Could not load " + directory + "/" + filename + ": " + e.getMessage());
            }
        }
    }

    /**
     * Attempt to locate and open a properties file. Searches:
     * 1. {directory}/{filename} relative to project root
     * 2. Classpath resource /{directory}/{filename}
     */
    public InputStream getInputStream(String filename, String directory)
    {
        // Try filesystem first
        Path filePath = Paths.get(directory, filename);
        if (Files.exists(filePath))
        {
            try
            {
                return new FileInputStream(filePath.toFile());
            }
            catch (Exception ignored)
            {
                // Fall through to classpath
            }
        }

        // Try classpath
        String resourcePath = "/" + directory + "/" + filename;
        InputStream classpathStream = getClass().getResourceAsStream(resourcePath);
        if (classpathStream != null)
        {
            return classpathStream;
        }

        // Try without leading slash
        return getClass().getClassLoader().getResourceAsStream(directory + "/" + filename);
    }

    // ---------------------------------------------------------------------------
    // Logging Helpers — instance methods delegating to Log
    // ---------------------------------------------------------------------------

    public void logComment(String message)  { Log.comment(this, message); }
    public void logPass(String message)     { Log.pass(this, message); }
    public void logFail(String message)     { Log.fail(this, message); }
    public void logWarning(String message)  { Log.warning(this, message); }
    public void logStep(String message)     { Log.step(this, message); }
}
