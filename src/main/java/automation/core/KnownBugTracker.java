package automation.core;

import automation.core.TestDataReader;
import automation.core.Log;

import java.util.*;

/**
 * Known bug tracking with JIRA integration.
 * Loads known failure patterns from CSV and auto-marks matching failures.
 */
public class KnownBugTracker
{

    private static List<Map<String, String>> knownBugs = null;

    public static void loadKnownBugs(String csvPath)
    {
        knownBugs = TestDataReader.readCsv(csvPath);
        Log.info("Loaded " + knownBugs.size() + " known bugs from: " + csvPath);
    }

    /**
     * Check if a test failure matches a known bug
     * @return JIRA ticket ID if known bug, null otherwise
     */
    public static String checkKnownBug(String testName, String errorMessage)
    {
        if (knownBugs == null || knownBugs.isEmpty()) return null;

        for (Map<String, String> bug : knownBugs)
        {
            String bugTestName = bug.getOrDefault("testName", "");
            String bugPattern = bug.getOrDefault("errorPattern", "");
            String jiraId = bug.getOrDefault("jiraId", "");

            boolean testMatch = bugTestName.isEmpty() || testName.contains(bugTestName);
            boolean errorMatch = bugPattern.isEmpty() || (errorMessage != null && errorMessage.contains(bugPattern));

            if (testMatch && errorMatch)
            {
                return jiraId;
            }
        }
        return null;
    }

}
