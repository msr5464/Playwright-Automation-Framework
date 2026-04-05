package automation.core.mobile;

import automation.core.Config;
import automation.core.Log;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Initializes an Appium driver for Android or iOS testing.
 * Supports three execution modes:
 *   1. Local device — starts Appium server via StartStopAppiumServer
 *   2. In-house device farm — connects to appiumDeviceFarmUrl
 *   3. BrowserStack — reads mobileConfiguration.json and sets bstack:options
 */
public class AppiumDriverManager
{

    /**
     * Creates and returns an AppiumDriver (AndroidDriver or IOSDriver) based on
     * the mobilePlatform property ("android" or "ios") and execution mode flags.
     * 
     * Note: AndroidDriver and IOSDriver constructors have a known compatibility issue with
     * Appium 9.3.0 / Selenium 4.27.0 where they expect HttpClient$Factory instead of URL.
     * This is a transitive dependency issue that will be resolved in future updates.
     */
    public AppiumDriver mobileDriver(Config testConfig)
    {
        String paramPath = System.getProperty("user.dir") + File.separator + "parameters" + File.separator;
        String remoteUrl;

        testConfig.isAndroid = "android".equalsIgnoreCase(testConfig.getRunTimeProperty("mobilePlatform"));
        testConfig.isIos     = "ios".equalsIgnoreCase(testConfig.getRunTimeProperty("mobilePlatform"));

        // Determine connection URL
        if (Config.isRemoteExecution && !Config.isBrowserStackExecution)
        {
            Log.comment(testConfig, "Connecting to in-house device farm");
            remoteUrl = testConfig.getRunTimeProperty("appiumDeviceFarmUrl");
        }
        else if (Config.isRemoteExecution && Config.isBrowserStackExecution)
        {
            Log.comment(testConfig, "Connecting to BrowserStack device farm");
            remoteUrl = testConfig.getRunTimeProperty("browserStackDeviceFarmUrl");
        }
        else
        {
            Log.comment(testConfig, "Starting local Appium server");
            StartStopAppiumServer.startAppiumServer(testConfig);
            remoteUrl = testConfig.appiumServer.getUrl().toString();
        }

        // Build capabilities
        UiAutomator2Options androidOptions = null;
        XCUITestOptions iosOptions = null;

        if (Config.isBrowserStackExecution)
        {
            if (testConfig.isAndroid)
            {
                androidOptions = new UiAutomator2Options();
                BrowserStackHelper.readDesireCapabilities(testConfig, paramPath, androidOptions);
            }
            else
            {
                iosOptions = new XCUITestOptions();
                BrowserStackHelper.readDesireCapabilities(testConfig, paramPath, iosOptions);
            }
        }
        else
        {
            String propsFile = paramPath + (testConfig.isAndroid ? "android.properties" : "ios.properties");
            Properties props = loadPropertiesFile(testConfig, propsFile);
            Pattern envVarPattern = Pattern.compile("\\$\\{(.+?)}");

            if (testConfig.isAndroid) androidOptions = new UiAutomator2Options();
            else iosOptions = new XCUITestOptions();

            if (props != null)
            {
                for (String key : props.stringPropertyNames())
                {
                    String value = props.getProperty(key);
                    Matcher matcher = envVarPattern.matcher(value);
                    if (matcher.find())
                    {
                        value = matcher.replaceAll(matchResult ->
                        {
                            String envVar = System.getenv(matchResult.group(1));
                            return envVar != null ? envVar.replace("\\", "\\\\") : matchResult.group(0);
                        });
                    }

                    if (testConfig.isAndroid) androidOptions.setCapability(key, value);
                    else iosOptions.setCapability(key, value);
                }
            }

            // Set app path for remote in-house farm
            if (Config.isRemoteExecution)
            {
                String buildSuffix = testConfig.isAndroid ? "android/build/app-sg-release.apk" : "ios/Jarvis.ipa";
                String appPath = testConfig.getRunTimeProperty("appBuildOnDeviceFarmPath") + buildSuffix;
                if (testConfig.isAndroid) androidOptions.setApp(appPath);
                else iosOptions.setApp(appPath);
            }
        }

        // Instantiate driver
        try
        {
            URL driverUrl = new URI(remoteUrl).toURL();
            if (testConfig.isAndroid)
            {
                Log.comment(testConfig, "Starting AndroidDriver on: " + remoteUrl);
                return new AndroidDriver(driverUrl, androidOptions);
            }
            else
            {
                Log.comment(testConfig, "Starting IOSDriver on: " + remoteUrl);
                return new IOSDriver(driverUrl, iosOptions);
            }
        }
        catch (URISyntaxException | MalformedURLException e)
        {
            throw new RuntimeException("Invalid Appium URL: " + remoteUrl, e);
        }
    }

    private Properties loadPropertiesFile(Config testConfig, String path)
    {
        try (FileInputStream fis = new FileInputStream(path))
        {
            Properties props = new Properties();
            props.load(fis);
            return props;
        }
        catch (Exception e)
        {
            Log.comment(testConfig, "Could not load mobile properties from: " + path + " - " + e.getMessage());
            return null;
        }
    }
}
