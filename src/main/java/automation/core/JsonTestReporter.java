package automation.core;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TestNG listener that writes machine-readable JSON results to test-results/report.json.
 *
 * AI agents should read this file to understand which tests failed, why they failed,
 * and where the failure screenshot is located — without parsing HTML reports.
 *
 * Output format (one JSON array written at suite end):
 * [
 *   {
 *     "testName":        "createAndVerifyCard",
 *     "className":       "automation.cards.CardApiTest",
 *     "status":          "FAILED",
 *     "failureMessage":  "Expected [foo] but got [bar]",
 *     "failureLocation": "CardApiTest.java:42",
 *     "durationMs":      4200,
 *     "retryCount":      1,
 *     "screenshotPath":  "test-results/screenshots/createAndVerifyCard_1234.png",
 *     "groups":          ["regression", "apiCases"],
 *     "country":         "SG",
 *     "automatedBy":     "Mukesh",
 *     "feature":         "CARD",
 *     "testType":        "HAPPY_PATH",
 *     "description":     "Create a card and verify it is returned by the list endpoint",
 *     "timestamp":       "2026-03-29T10:00:00Z"
 *   }
 * ]
 *
 * Registered in testng.xml alongside TestListener.
 */
public class JsonTestReporter implements ITestListener
{

    private static final String OUTPUT_DIR  = "test-results";
    private static final String OUTPUT_FILE = OUTPUT_DIR + "/report.json";

    /** Thread-safe list of JSON entries accumulated during the suite run. */
    private final List<String> entries = new CopyOnWriteArrayList<>();

    // ─── ITestListener callbacks ──────────────────────────────────────────────

    @Override
    public void onTestSuccess(ITestResult result)
    {
        entries.add(buildEntry(result, "PASSED", null));
    }

    @Override
    public void onTestFailure(ITestResult result)
    {
        entries.add(buildEntry(result, "FAILED", result.getThrowable()));
    }

    @Override
    public void onTestSkipped(ITestResult result)
    {
        entries.add(buildEntry(result, "SKIPPED", result.getThrowable()));
    }

    @Override
    public void onFinish(ITestContext context)
    {
        writeReport();
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private String buildEntry(ITestResult result, String status, Throwable throwable)
    {
        String testName   = result.getMethod().getMethodName();
        String className  = result.getTestClass().getName();
        long   durationMs = result.getEndMillis() - result.getStartMillis();
        int    retryCount = result.getMethod().getCurrentInvocationCount() - 1;
        String timestamp  = Instant.now().toString();

        // Groups
        String[] groupArr = result.getMethod().getGroups();
        String groups = jsonStringArray(groupArr);

        // @TestVariables metadata
        String country     = "";
        String automatedBy = "";
        try
        {
            Method method = result.getMethod().getConstructorOrMethod().getMethod();
            if (method.isAnnotationPresent(TestVariables.class))
            {
                TestVariables tv = method.getAnnotation(TestVariables.class);
                country     = tv.country().name();
                automatedBy = tv.automatedBy().name();
            }
        }
        catch (Exception ignored) { /* annotation may not be present */ }

        // Description — read from @Test(description = "...") where it already lives
        String description = result.getMethod().getDescription() != null
                ? result.getMethod().getDescription() : "";

        // Feature — derived from package name (e.g. automation.cards.CardApiTest → cards)
        // No manual annotation needed; package already encodes the module.
        String feature = "";
        String[] packageParts = className.split("\\.");
        if (packageParts.length >= 2)
        {
            feature = packageParts[packageParts.length - 2];
        }

        // Failure details
        String failureMessage  = "";
        String failureLocation = "";
        if (throwable != null)
        {
            failureMessage = escapeJson(throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getSimpleName());
            StackTraceElement[] trace = throwable.getStackTrace();
            if (trace != null && trace.length > 0)
            {
                // Find the first frame inside our test code
                for (StackTraceElement frame : trace)
                {
                    if (frame.getClassName().startsWith("automation."))
                    {
                        failureLocation = frame.getFileName() + ":" + frame.getLineNumber();
                        break;
                    }
                }
                if (failureLocation.isEmpty())
                {
                    failureLocation = trace[0].getFileName() + ":" + trace[0].getLineNumber();
                }
            }
        }

        // Screenshot path is populated by TestListener, not available here
        String screenshotPath = "";

        return "  {\n" +
               "    \"testName\": \""        + escapeJson(testName)       + "\",\n" +
               "    \"className\": \""       + escapeJson(className)      + "\",\n" +
               "    \"status\": \""          + status                     + "\",\n" +
               "    \"failureMessage\": \""  + failureMessage             + "\",\n" +
               "    \"failureLocation\": \"" + escapeJson(failureLocation) + "\",\n" +
               "    \"durationMs\": "        + durationMs                 + ",\n" +
               "    \"retryCount\": "        + retryCount                 + ",\n" +
               "    \"screenshotPath\": \""  + escapeJson(screenshotPath) + "\",\n" +
               "    \"groups\": "            + groups                     + ",\n" +
               "    \"country\": \""         + escapeJson(country)        + "\",\n" +
               "    \"automatedBy\": \""     + escapeJson(automatedBy)    + "\",\n" +
               "    \"feature\": \""         + escapeJson(feature)        + "\",\n" +
               "    \"description\": \""     + escapeJson(description)    + "\",\n" +
               "    \"timestamp\": \""       + timestamp                  + "\"\n" +
               "  }";
    }

    private void writeReport()
    {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < entries.size(); i++)
        {
            sb.append(entries.get(i));
            if (i < entries.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");

        try (FileWriter fw = new FileWriter(new File(OUTPUT_FILE)))
        {
            fw.write(sb.toString());
            System.out.println("[JsonTestReporter] Results written to: " + OUTPUT_FILE);
        }
        catch (IOException e)
        {
            System.err.println("[JsonTestReporter] Failed to write report: " + e.getMessage());
        }
    }

    private static String jsonStringArray(String[] values)
    {
        if (values == null || values.length == 0) return "[]";
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (String v : values) sj.add("\"" + escapeJson(v) + "\"");
        return sj.toString();
    }

    private static String escapeJson(String value)
    {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
