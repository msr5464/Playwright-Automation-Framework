package automation.core.mobile;

import automation.core.CmdHelper;
import automation.core.Config;
import automation.core.Log;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;

import java.io.File;
import java.io.IOException;

/**
 * Manages the lifecycle of a local Appium server for mobile test execution.
 * Use only when Config.isRemoteExecution = false (local device testing).
 */
public class StartStopAppiumServer
{

    public static void startAppiumServer(Config testConfig)
    {
        killAllAppiumServers(testConfig);

        String nodePath = CmdHelper.executeCommandAndWaitForOutput(testConfig, "npm root -g").trim();
        Log.comment(testConfig, "Starting local Appium server (npm root: " + nodePath + ")");

        int retryCount = 0;
        do
        {
            try
            {
                AppiumServiceBuilder builder = new AppiumServiceBuilder();
                builder.usingAnyFreePort();
                builder.withArgument(GeneralServerFlag.SESSION_OVERRIDE);
                builder.withArgument(GeneralServerFlag.LOG_LEVEL, "error");
                builder.withArgument(GeneralServerFlag.RELAXED_SECURITY);
                builder.withAppiumJS(new File(nodePath + "/appium/build/lib/main.js"));

                testConfig.appiumServer = AppiumDriverLocalService.buildService(builder);
                testConfig.appiumServer.start();
                Log.comment(testConfig, "Appium server started on: " + testConfig.appiumServer.getUrl());
                return;
            }
            catch (Exception e)
            {
                retryCount++;
                Log.comment(testConfig, "Failed to start Appium server (attempt " + retryCount + "/3): " + e.getMessage());
            }
        }
        while (retryCount < 3);

        throw new RuntimeException("Could not start Appium server after 3 attempts");
    }

    public static void stopAppiumServer(Config testConfig)
    {
        if (testConfig.appiumServer != null)
        {
            try
            {
                testConfig.appiumServer.stop();
                Log.comment(testConfig, "Appium server stopped");
            }
            catch (Exception e)
            {
                Log.comment(testConfig, "Error stopping Appium server: " + e.getMessage());
            }
        }
    }

    private static void killAllAppiumServers(Config testConfig)
    {
        if (Config.isRemoteExecution) return;

        try
        {
            if (Config.osName.contains("windows"))
            {
                Log.comment(testConfig, "Killing Appium servers on Windows");
                Runtime.getRuntime().exec(new String[]{"taskkill", "/f", "/im", "node.exe"});
            }
            else if (Config.osName.contains("mac"))
            {
                Log.comment(testConfig, "Killing Appium servers on Mac");
                Runtime.getRuntime().exec(new String[]{"/usr/bin/killall", "-KILL", "node"});
            }
            else
            {
                Log.comment(testConfig, "Killing Appium servers on Linux");
                Runtime.getRuntime().exec(new String[]{"pkill", "-f", "appium"});
            }
            Thread.sleep(1000);
        }
        catch (IOException | InterruptedException e)
        {
            Log.comment(testConfig, "Could not kill existing Appium servers: " + e.getMessage());
        }
    }
}
