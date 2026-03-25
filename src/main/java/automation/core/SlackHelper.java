package automation.core;

import automation.core.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Slack notification helper with thread-based reporting.
 */
public class SlackHelper
{

    public static void sendMessage(String webhookUrl, String message)
    {
        try
        {
            String payload = "{\"text\": \"" + escapeJson(message) + "\"}";
            postToSlack(webhookUrl, payload);
            Log.info("Slack message sent");
        }
        catch (Exception e)
        {
            Log.error("Slack notification failed: " + e.getMessage());
        }
    }

    public static void sendTestReport(String webhookUrl, String suiteName, int passed, int failed, int skipped, String buildUrl)
    {
        int total = passed + failed + skipped;
        double passRate = total > 0 ? (double) passed / total * 100 : 0;

        String message = String.format(
            "*Test Report: %s*\n" +
            "Total: %d | Passed: %d | Failed: %d | Skipped: %d\n" +
            "Pass Rate: %.1f%%\n" +
            "%s",
            suiteName, total, passed, failed, skipped, passRate,
            buildUrl != null ? "Build: " + buildUrl : ""
        );
        sendMessage(webhookUrl, message);
    }

    private static void postToSlack(String webhookUrl, String payload) throws Exception
    {
        URL url = new URL(webhookUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream())
        {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200)
        {
            Log.error("Slack API returned: " + responseCode);
        }
        conn.disconnect();
    }

    private static String escapeJson(String text)
    {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
