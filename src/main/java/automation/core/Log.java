package automation.core;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.Reporter;

public class Log {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ANSI color codes for console
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    //private static final String ORANGE = "\u001B[38;5;208m";

    private static String timestamp() {
        return "[" + LocalTime.now().format(TIME_FORMAT) + "]";
    }

    // ========== CONSOLE OUTPUT (conditional on remote execution) ==========

    private static void logToConsole(Config config, String message) {
        if (Config.isRemoteExecution && !Config.isDebugMode) {
            // Suppress console noise in CI when debug mode is off
            return;
        }
        if (Config.isRemoteExecution && Config.isDebugMode) {
            String testName = (config != null && config.testcaseName != null) ? "[" + config.testcaseName + "]" : "";
            System.out.println(testName + timestamp() + " " + message);
        } else {
            System.out.println(timestamp() + " " + message);
        }
    }

    // ========== STACK TRACE CLEANING ==========

    private static final String[] FILTERED_PACKAGES = {
            "automation.core.", "org.testng", "com.microsoft.playwright",
            "org.apache.maven", "sun.reflect", "jdk.internal", "java.lang", "java.util"
    };

    public static StackTraceElement[] getCleanedStackTraceElements(StackTraceElement[] trace) {
        if (trace == null)
            return new StackTraceElement[0];
        return Arrays.stream(trace)
                .filter(e -> {
                    String cls = e.getClassName();
                    for (String pkg : FILTERED_PACKAGES) {
                        if (cls.startsWith(pkg))
                            return false;
                    }
                    return true;
                })
                .toArray(StackTraceElement[]::new);
    }

    public static String getCleanedStackTraceString(StackTraceElement[] trace) {
        StackTraceElement[] cleaned = getCleanedStackTraceElements(trace);
        if (cleaned.length == 0)
            return "";
        return "\n" + Arrays.stream(cleaned)
                .map(e -> "  at " + e.toString())
                .collect(Collectors.joining("\n"));
    }

    // ========== CONSTANTS FOR HTML STYLING ==========
    private static final String FONT_SANS = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif";
    private static final String FONT_MONO = "ui-monospace, SFMono-Regular, Consolas, monospace";

    // ========== STEP (orange background - test steps) ==========

    public static void step(Config config, String message) {
        logToConsole(config, BLUE + "STEP: " + message + RESET);
        String badge = "<span style=\"background-color:#3B82F6;color:white;padding:2px 8px;border-radius:4px;font-weight:600;font-size:0.85em;\">STEP</span>";
        logToReporter(config, badge, "<strong style=\"color:#111827;font-weight:600;\">" + message + "</strong>", null);
    }

    // ========== PASS (green text) ==========

    public static void pass(Config config, String message) {
        logToConsole(config, GREEN + message + RESET);
        String msgHtml = "<span style=\"color:#10B981;\">" + message + "</span>";
        logToReporter(config, null, msgHtml, null);
    }

    // ========== FAIL — captures call-site trace, delegates to failure() ==========

    public static void fail(Config config, String message) {
        failure(config, message, new Throwable());
        BrowserHelper.takeScreenshot(config);
    }

    // ========== FAILURE — logs with collapsible stack trace, respects endExecutionOnFailure ==========

    public static void failure(Config config, String message, Throwable throwable) {
        String traceStr = (throwable != null) ? getCleanedStackTraceString(throwable.getStackTrace()) : "";

        logToConsole(config, RED + message + RESET);

        String msgHtml;
        if (!traceStr.isEmpty()) {
            String uniqueId = "fail_" + System.nanoTime();
            msgHtml = "<a href=\"javascript:void(0);\" onclick=\"var el=document.getElementById('" + uniqueId + "');"
                + "el.style.display=el.style.display==='none'?'block':'none';\" "
                + "style=\"color:#EF4444;font-weight:600;text-decoration:none;\">"
                + escapeHtml(message) + "</a>"
                + "<pre id=\"" + uniqueId + "\" style=\"display:none;background-color:#FEE2E2;color:#991B1B;font-family:" + FONT_MONO
                + ";font-size:0.85em;padding:8px;border-radius:6px;border:1px solid #FCA5A5;overflow:auto;max-height:200px;margin:4px 0 0 0;\">"
                + escapeHtml(traceStr.trim()) + "</pre>";
        } else {
            msgHtml = "<span style=\"color:#EF4444;font-weight:600;\">" + escapeHtml(message) + "</span>";
        }
        logToReporter(config, null, msgHtml, null);

        if (config != null) {
            if (config.endExecutionOnFailure) {
                Assert.fail(message);
            } else {
                config.softAssert.assertTrue(false, message);
            }
        }
    }

    // ========== WARNING (orange text) ==========

    public static void warning(Config config, String message) {
        logToConsole(config, YELLOW + "WARNING: " + message + RESET);
        String badge = "<span style=\"color:#F59E0B;font-weight:700;font-size:0.9em;\">&#9888; WARNING:</span>";
        logToReporter(config, badge, message, null);
    }

    public static void warning(Config config, String message, Throwable throwable) {
        String traceStr = (throwable != null) ? getCleanedStackTraceString(throwable.getStackTrace()) : "";
        String fullMessage = message + traceStr;
        logToConsole(config, YELLOW + "WARNING: " + fullMessage + RESET);

        String badge = "<span style=\"color:#F59E0B;font-weight:700;font-size:0.9em;\">&#9888; WARNING:</span>";
        String details = null;
        if (!traceStr.isEmpty()) {
            details = "<pre style=\"background-color:#FEF3C7;color:#92400E;font-family:" + FONT_MONO
                    + ";font-size:0.9em;padding:8px;border-radius:6px;border:1px solid #FCD34D;overflow:auto;margin:0;\">"
                    + escapeHtml(traceStr.trim()) + "</pre>";
        }

        logToReporter(config, badge, escapeHtml(message), details);
    }

    // ========== COMMENT (default text) ==========

    public static void comment(Config config, String message) {
        logToConsole(config, message);
        logToReporter(config, null, message, null);
    }

    public static void comment(Config config, String message, String color) {
        logToConsole(config, message);
        String styledMsg = "<span style=\"color:" + color + ";\">" + message + "</span>";
        logToReporter(config, null, styledMsg, null);
    }

    // ========== ACTION (for UI interactions) ==========

    public static void action(Config config, String message) {
        logToConsole(config, CYAN + "ACTION: " + message + RESET);
        String badge = "<span style=\"color:#14B8A6;font-weight:700;font-size:0.9em;\">ACTION:</span>";
        logToReporter(config, badge, message, null);
    }

    // ========== LINK (hyperlinked text in reports) ==========

    public static void link(Config config, String text, String url) {
        logToConsole(config, "LINK: " + text + " -> " + url);
        String linkHtml = "<a href=\"" + url
                + "\" target=\"_blank\" style=\"color:#3B82F6;text-decoration:none;font-weight:500;\" onmouseover=\"this.style.textDecoration='underline'\" onmouseout=\"this.style.textDecoration='none'\">"
                + text + "</a>";
        logToReporter(config, null, linkHtml, null);
    }

    // ========== JSON (collapsible JSON in reports) ==========

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private static String prettyPrintJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return jsonString;
        }
        try {
            Object json = OBJECT_MAPPER.readValue(jsonString, Object.class);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return jsonString; // return original if not valid JSON
        }
    }

    public static void commentJson(Config config, String label, String jsonContent) {
        String formattedJson = prettyPrintJson(jsonContent);
        logToConsole(config, label + ":\n" + formattedJson);
        String uniqueId = "json_" + System.nanoTime();

        String mainHtml = "<strong style=\"font-weight:600;\">" + label + ":</strong> " +
                "<a href=\"javascript:void(0);\" onclick=\"var el=document.getElementById('" + uniqueId
                + "');el.style.display=el.style.display==='none'?'block':'none';\" style=\"color:#6B7280;text-decoration:none;font-size:0.9em;padding:2px 6px;background:#E5E7EB;border-radius:4px;\">Toggle Data &#9660;</a>";

        String details = "<pre id=\"" + uniqueId
                + "\" style=\"display:none;background-color:#F3F4F6;color:#1F2937;font-family:" + FONT_MONO
                + ";font-size:0.9em;padding:8px;border:1px solid #D1D5DB;border-radius:6px;overflow:auto;max-height:400px;margin:0;\">"
                + escapeHtml(formattedJson) + "</pre>";

        logToReporter(config, null, mainHtml, details);
    }

    public static void debug(Config config, String message) {
        if (Config.isDebugMode) {
            logToConsole(config, "DEBUG: " + message);
            String badge = "<span style=\"color:#9CA3AF;font-style:italic;font-weight:700;font-size:0.9em;\">DEBUG:</span>";
            String msgHtml = "<span style=\"color:#6B7280;font-style:italic;\">" + message + "</span>";
            logToReporter(config, badge, msgHtml, null);
        }
    }

    private static void logToReporter(Config config, String badge, String mainContent, String details) {
        StringBuilder sb = new StringBuilder();

        // Main Container
        sb.append("<div style=\"font-family: ").append(FONT_SANS)
                .append("; font-size: 12px; line-height: 1.3; margin: 3px 0; display: flex; align-items: flex-start; gap: 6px;\">");

        // Timestamp
        sb.append("<span style=\"color: #6B7280; font-size: 0.85em; font-family: ").append(FONT_MONO)
                .append("; white-space: nowrap; margin-top: 2px;\">")
                .append(timestamp()).append("</span>");

        // Content Area Container
        sb.append("<div style=\"flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px;\">");

        // Badge + Message Row
        sb.append("<div style=\"display: flex; align-items: baseline; gap: 8px; flex-wrap: wrap;\">");
        if (badge != null && !badge.isEmpty()) {
            sb.append(badge);
        }
        if (mainContent != null && !mainContent.isEmpty()) {
            // Apply word-wrap so long strings without whitespace don't break flex layout
            sb.append("<span style=\"color: #374151; word-break: break-word;\">").append(mainContent).append("</span>");
        }
        sb.append("</div>");

        // Details Row
        if (details != null && !details.isEmpty()) {
            sb.append("<div style=\"width: 100%;\">").append(details).append("</div>");
        }

        sb.append("</div></div>");

        String finalHtml = sb.toString();
        try {
            Reporter.log(finalHtml);
        } catch (Exception ignored) {
            // Reporter may not be available outside TestNG context
        }
        if (config != null) {
            config.testLog += finalHtml;
        }

        // Forward plain-text version to ChainTest so logs appear in its report
        try {
            if (mainContent != null && !mainContent.isEmpty()) {
                String plainText = mainContent.replaceAll("<[^>]+>", "").trim();
                if (!plainText.isEmpty()) {
                    String prefix = (badge != null && !badge.isEmpty())
                        ? badge.replaceAll("<[^>]+>", "").trim() + " " : "";
                    com.aventstack.chaintest.plugins.ChainTestListener.log(prefix + plainText);
                }
            }
        } catch (Exception ignored) {
            // ChainTest may not be active in all execution contexts
        }
    }

    public static String escapeHtml(String text) {
        if (text == null)
            return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static void info(String message) {
        System.out.println(timestamp() + " " + BLUE + "INFO: " + message + RESET);
    }

    public static void error(String message) {
        System.out.println(timestamp() + " " + RED + "ERROR: " + message + RESET);
    }
}
