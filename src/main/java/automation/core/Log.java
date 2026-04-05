package automation.core;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.testng.Reporter;

public class Log
{

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ANSI color codes for console
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String ORANGE = "\u001B[38;5;208m";

    private static String timestamp()
    {
        return "[" + LocalTime.now().format(TIME_FORMAT) + "]";
    }

    // ========== STEP (orange background - test steps) ==========

    public static void step(Config config, String message)
    {
        String consoleMsg = timestamp() + " " + ORANGE + "STEP: " + message + RESET;
        System.out.println(consoleMsg);
        String htmlMsg = "<br><b style='background-color:#FF8C00;color:white;padding:2px 6px;border-radius:3px;'>STEP</b> " + message;
        logToReporter(config, htmlMsg);
    }

    // ========== PASS (green text) ==========

    public static void pass(Config config, String message)
    {
        String consoleMsg = timestamp() + " " + GREEN + "PASS: " + message + RESET;
        System.out.println(consoleMsg);
        String htmlMsg = "<br><span style='color:green;font-weight:bold;'>&#10004; PASS:</span> " + message;
        logToReporter(config, htmlMsg);
    }

    // ========== FAIL (red text) ==========

    public static void fail(Config config, String message)
    {
        String consoleMsg = timestamp() + " " + RED + "FAIL: " + message + RESET;
        System.out.println(consoleMsg);
        String htmlMsg = "<br><span style='color:red;font-weight:bold;'>&#10008; FAIL:</span> " + message;
        logToReporter(config, htmlMsg);
    }

    // ========== WARNING (orange text) ==========

    public static void warning(Config config, String message)
    {
        String consoleMsg = timestamp() + " " + YELLOW + "WARNING: " + message + RESET;
        System.out.println(consoleMsg);
        String htmlMsg = "<br><span style='color:orange;font-weight:bold;'>&#9888; WARNING:</span> " + message;
        logToReporter(config, htmlMsg);
    }

    // ========== COMMENT (default text) ==========

    public static void comment(Config config, String message)
    {
        String consoleMsg = timestamp() + " " + RESET + message + RESET;
        System.out.println(consoleMsg);
        String htmlMsg = "<br>" + message;
        logToReporter(config, htmlMsg);
    }

    public static void comment(Config config, String message, String color)
    {
        String consoleMsg = timestamp() + " " + message;
        System.out.println(consoleMsg);
        String htmlMsg = "<br><span style='color:" + color + ";'>" + message + "</span>";
        logToReporter(config, htmlMsg);
    }

    // ========== VERIFY (blue text - assertions/verifications) ==========

    public static void verify(Config config, String message)
    {
        String consoleMsg = timestamp() + " " + BLUE + "VERIFY: " + message + RESET;
        System.out.println(consoleMsg);
        String htmlMsg = "<br><span style='color:blue;font-weight:bold;'>&#128270; VERIFY:</span> " + message;
        logToReporter(config, htmlMsg);
    }

    // ========== ACTION (for UI interactions) ==========

    public static void action(Config config, String message)
    {
        String consoleMsg = timestamp() + " " + CYAN + "ACTION: " + message + RESET;
        System.out.println(consoleMsg);
        String htmlMsg = "<br><span style='color:teal;'>&#9654; ACTION:</span> " + message;
        logToReporter(config, htmlMsg);
    }

    // ========== LINK (hyperlinked text in reports) ==========

    public static void link(Config config, String text, String url)
    {
        String consoleMsg = timestamp() + " LINK: " + text + " -> " + url;
        System.out.println(consoleMsg);
        String htmlMsg = "<br><a href='" + url + "' target='_blank' style='color:blue;'>" + text + "</a>";
        logToReporter(config, htmlMsg);
    }

    // ========== JSON (collapsible JSON in reports) ==========

    public static void commentJson(Config config, String label, String jsonContent)
    {
        String consoleMsg = timestamp() + " " + label + ": " + jsonContent;
        System.out.println(consoleMsg);
        String uniqueId = "json_" + System.currentTimeMillis();
        String htmlMsg = "<br><b>" + label + ":</b> " +
            "<button onclick=\"var el=document.getElementById('" + uniqueId + "');el.style.display=el.style.display==='none'?'block':'none';\" " +
            "style='cursor:pointer;background:#eee;border:1px solid #ccc;padding:2px 8px;border-radius:3px;'>Expand/Collapse</button>" +
            "<pre id='" + uniqueId + "' style='display:none;background:#f5f5f5;padding:10px;border:1px solid #ddd;border-radius:4px;overflow:auto;max-height:400px;'>" +
            escapeHtml(jsonContent) + "</pre>";
        logToReporter(config, htmlMsg);
    }

    public static void debug(Config config, String message)
    {
        if (Config.isDebugMode)
        {
            String consoleMsg = timestamp() + " DEBUG: " + message;
            System.out.println(consoleMsg);
            String htmlMsg = "<br><span style='color:gray;font-style:italic;'>DEBUG: " + message + "</span>";
            logToReporter(config, htmlMsg);
        }
    }

    private static void logToReporter(Config config, String htmlMsg)
    {
        try
        {
            Reporter.log(htmlMsg);
        }
        catch (Exception ignored)
        {
            // Reporter may not be available outside TestNG context
        }
        if (config != null)
        {
            config.testLog += htmlMsg;
        }
    }

    private static String escapeHtml(String text)
    {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    public static void info(String message)
    {
        System.out.println(timestamp() + " " + BLUE + "INFO: " + message + RESET);
    }

    public static void error(String message)
    {
        System.out.println(timestamp() + " " + RED + "ERROR: " + message + RESET);
    }
}
