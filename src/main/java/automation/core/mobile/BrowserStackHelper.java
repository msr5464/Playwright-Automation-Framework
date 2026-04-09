package automation.core.mobile;

import automation.core.Config;
import automation.core.Log;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Random;

/**
 * BrowserStack cloud device farm integration.
 *
 * Capabilities are loaded from parameters/mobileConfiguration.json.
 * BrowserStack credentials are read from system properties:
 *   -DbrowserStackUserName=<username> -DbrowserStackAccessKey=<key>
 *
 * mobileConfiguration.json format:
 * {
 *   "android": {
 *     "devices": [{"deviceName":"Samsung Galaxy S22","platformVersion":"12.0"}],
 *     "desired_capabilities": {
 *       "platformName": "Android",
 *       "browserstackOptions": {"projectName": "Jarvis", "networkLogs": true}
 *     }
 *   },
 *   "ios": { ... }
 * }
 */
public class BrowserStackHelper
{
    private static final String SESSION_API_BASE = "https://api-cloud.browserstack.com/app-automate/sessions/";

    /**
     * Configure UiAutomator2Options for an Android BrowserStack session.
     */
    public static void readDesireCapabilities(Config testConfig, String paramPath, UiAutomator2Options options)
    {
        setupCapabilities(testConfig, paramPath, options, "android");
    }

    /**
     * Configure XCUITestOptions for an iOS BrowserStack session.
     */
    public static void readDesireCapabilities(Config testConfig, String paramPath, XCUITestOptions options)
    {
        setupCapabilities(testConfig, paramPath, options, "ios");
    }

    private static void setupCapabilities(Config testConfig, String paramPath, Object options, String platform)
    {
        JSONObject mobileConfig = readJsonFile(testConfig, paramPath + "mobileConfiguration.json");
        if (mobileConfig == null) return;

        JSONObject platformConfig = mobileConfig.getJSONObject(platform);
        JSONArray devicesList     = platformConfig.getJSONArray("devices");
        JSONObject desiredCaps    = platformConfig.getJSONObject("desired_capabilities");

        HashMap<String, Object> bstackOptions = new HashMap<>();
        HashMap<String, Object> networkOptions = new HashMap<>();

        // Process desired capabilities
        for (String capKey : desiredCaps.keySet())
        {
            Object capValue = desiredCaps.get(capKey);
            Log.comment(testConfig, "[BrowserStack] Cap: " + capKey + " = " + capValue);

            if (capValue instanceof JSONObject)
            {
                JSONObject jsonVal = (JSONObject) capValue;
                if ("browserstackOptions".equalsIgnoreCase(capKey))
                {
                    for (String k : jsonVal.keySet()) bstackOptions.put(k, jsonVal.get(k));
                }
                else if ("networkLogsOptions".equalsIgnoreCase(capKey))
                {
                    for (String k : jsonVal.keySet()) networkOptions.put(k, jsonVal.get(k));
                }
            }
            else
            {
                setCapability(options, capKey, capValue);
            }
        }

        // BrowserStack authentication
        String bsUser = System.getProperty("browserStackUserName", "");
        String bsKey  = System.getProperty("browserStackAccessKey", "");
        bstackOptions.put("userName", bsUser);
        bstackOptions.put("accessKey", bsKey);

        // Build name and session name
        String buildName = testConfig.getRunTimeProperty("projectName", "Jarvis")
                + "_" + platform.toUpperCase()
                + "_" + LocalDate.now();
        bstackOptions.put("buildName", buildName);
        bstackOptions.put("sessionName", testConfig.testcaseName);

        // Pick a random device from the list
        JSONObject device = devicesList.getJSONObject(new Random().nextInt(devicesList.length()));
        String deviceName     = device.getString("deviceName");
        String platformVersion = device.getString("platformVersion");
        testConfig.putRunTimeProperty("deviceNameUnderTest", deviceName);
        Log.comment(testConfig, "[BrowserStack] Connecting to: " + deviceName + " (" + platformVersion + ")");

        // Apply device + bstack options
        if (options instanceof UiAutomator2Options androidOpts)
        {
            androidOpts.setDeviceName(deviceName);
            androidOpts.setPlatformVersion(platformVersion);
            androidOpts.setCapability("browserstack.networkLogsOptions", networkOptions);
            androidOpts.setCapability("bstack:options", bstackOptions);
        }
        else if (options instanceof XCUITestOptions iosOpts)
        {
            iosOpts.setDeviceName(deviceName);
            iosOpts.setPlatformVersion(platformVersion);
            iosOpts.setCapability("browserstack.networkLogsOptions", networkOptions);
            iosOpts.setCapability("bstack:options", bstackOptions);
        }
    }

    /**
     * Updates the BrowserStack session status (passed/failed) after the test completes.
     */
    public static void updateTestCaseStatus(Config testConfig, ITestResult result)
    {
        if (testConfig.appiumDriver == null) return;

        try
        {
            String reason = result.getThrowable() != null
                    ? result.getThrowable().toString()
                    : "Test executed successfully";
            String status = result.getStatus() == ITestResult.SUCCESS ? "passed" : "failed";

            String script = "browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \""
                    + status + "\", \"reason\":\"" + escapeJson(reason) + "\"}}";

            ((org.openqa.selenium.JavascriptExecutor) testConfig.appiumDriver).executeScript(script);
            Log.comment(testConfig, "BrowserStack session marked as: " + status);
        }
        catch (Exception e)
        {
            Log.comment(testConfig, "Failed to update BrowserStack session status: " + e.getMessage());
        }
    }

    /**
     * Fetches the BrowserStack session details (including video URL) via API.
     */
    public JSONObject getSessionDetails(Config testConfig)
    {
        if (testConfig.appiumDriver == null) return null;

        String sessionId = testConfig.appiumDriver.getSessionId().toString();
        String apiUrl    = SESSION_API_BASE + sessionId + ".json";
        String username  = System.getProperty("browserStackUserName", "");
        String accessKey = System.getProperty("browserStackAccessKey", "");

        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, accessKey));

        try (CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(provider).build())
        {
            HttpResponse response = client.execute(new HttpGet(apiUrl));
            String body = EntityUtils.toString(response.getEntity());
            return new JSONObject(body);
        }
        catch (IOException e)
        {
            Log.comment(testConfig, "Failed to fetch BrowserStack session details: " + e.getMessage());
            return null;
        }
    }

    /**
     * Logs the BrowserStack video recording link to the test report.
     */
    public void addSessionVideoLink(Config testConfig, JSONObject sessionDetails)
    {
        if (sessionDetails != null && sessionDetails.has("automation_session"))
        {
            String videoUrl = sessionDetails.getJSONObject("automation_session").getString("video_url");
            Log.comment(testConfig, "<a href='" + videoUrl + "' target='_blank' style='display:inline-block;padding:2px 8px;background-color:#E5E7EB;color:#2563EB;text-decoration:none;border-radius:4px;font-size:0.85em;font-weight:500;'>&#127909; View BrowserStack Video Recording</a>");
        }
        else
        {
            Log.comment(testConfig, "BrowserStack session details unavailable: " + sessionDetails);
        }
    }

    // ===== Helpers =====

    private static void setCapability(Object options, String key, Object value)
    {
        if (options instanceof UiAutomator2Options o) o.setCapability(key, value);
        else if (options instanceof XCUITestOptions o) o.setCapability(key, value);
    }

    private static JSONObject readJsonFile(Config testConfig, String path)
    {
        try
        {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            return new JSONObject(content);
        }
        catch (Exception e)
        {
            Log.comment(testConfig, "Could not read mobile configuration from: " + path + " - " + e.getMessage());
            return null;
        }
    }

    private static String escapeJson(String input)
    {
        return input == null ? "" : input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
