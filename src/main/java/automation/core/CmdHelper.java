package automation.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Utility for executing shell/batch commands from test code.
 * Supports both Mac/Linux (bash) and Windows (cmd.exe).
 */
public class CmdHelper
{

    /**
     * Executes a shell command, waits for it to complete, and returns the full output.
     *
     * @param testConfig - Config instance for logging (may be null for static use)
     * @param cmd        - command to execute
     * @return full stdout output of the command
     */
    public static String executeCommandAndWaitForOutput(Config testConfig, String cmd)
    {
        String fullLogs = "";
        Log.comment(testConfig, "Executing shell command: " + cmd);
        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (Config.osName.contains("windows"))
            {
                processBuilder.command("cmd.exe", "/c", cmd);
            }
            else
            {
                processBuilder.command("bash", "-c", cmd);
            }

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            Log.comment(testConfig, "======================================================");
            while ((line = reader.readLine()) != null)
            {
                fullLogs = fullLogs.concat("\n" + line);
                Log.comment(testConfig, line);
            }
            Log.comment(testConfig, "======================================================");
            int exitCode = process.waitFor();
            if (exitCode != 0)
            {
                Log.comment(testConfig, "Command exited with error code: " + exitCode);
            }
        }
        catch (Exception e)
        {
            Log.comment(testConfig, "Unable to execute shell command: " + cmd);
            e.printStackTrace();
        }
        return fullLogs;
    }

    /**
     * Executes a shell command and exits without capturing output.
     * Waits up to 10 seconds for the command to complete.
     *
     * @param testConfig - Config instance for logging (may be null)
     * @param cmd        - command to execute
     */
    public static void executeCommandAndExit(Config testConfig, String cmd)
    {
        Log.comment(testConfig, "Executing shell command: " + cmd);
        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (Config.osName.contains("windows"))
            {
                processBuilder.command("cmd.exe", "/c", cmd);
            }
            else
            {
                processBuilder.command("bash", "-c", cmd);
            }

            Process process = processBuilder.start();
            boolean completed = process.waitFor(10, TimeUnit.SECONDS);
            if (completed)
            {
                Log.comment(testConfig, "Command completed successfully: " + cmd);
            }
            else
            {
                Log.comment(testConfig, "Command timed out after 10s: " + cmd);
            }
        }
        catch (Exception e)
        {
            Log.comment(testConfig, "Unable to execute shell command: " + cmd);
            e.printStackTrace();
        }
    }
}
