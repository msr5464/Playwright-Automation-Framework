package helpers;

import com.microsoft.playwright.Locator;

public class AssertHelper {

    public static void compareEquals(Config config, String description, String expected, String actual) {
        try {
            config.logComment("Comparing " + description + ": Expected='" + expected + "', Actual='" + actual + "'");
            if (expected.equals(actual)) {
                config.logPass("PASS: " + description + " matches expected value");
            } else {
                config.logFail("FAIL: " + description + " does not match. Expected='" + expected + "', Actual='"
                        + actual + "'");
            }
        } catch (Exception e) {
            config.logExceptionAndFail("Error comparing " + description, e);
        }
    }

    public static void compareContains(Config config, String description, String expected, String actual) {
        try {
            config.logComment("Checking if " + description + " contains expected text: '" + expected + "'");
            if (actual != null && actual.contains(expected)) {
                config.logPass("PASS: " + description + " contains expected text");
            } else {
                config.logFail("FAIL: " + description + " does not contain expected text. Expected='" + expected
                        + "', Actual='" + actual + "'");
            }
        } catch (Exception e) {
            config.logExceptionAndFail("Error checking contains for " + description, e);
        }
    }

    public static void assertTrue(Config config, String description, boolean condition) {
        try {
            config.logComment("Asserting " + description + ": " + condition);
            if (condition) {
                config.logPass("PASS: " + description + " is true");
            } else {
                config.logFail("FAIL: " + description + " is false");
            }
        } catch (Exception e) {
            config.logExceptionAndFail("Error asserting " + description, e);
        }
    }

    public static void assertFalse(Config config, String description, boolean condition) {
        try {
            config.logComment("Asserting " + description + ": " + condition);
            if (!condition) {
                config.logPass("PASS: " + description + " is false");
            } else {
                config.logFail("FAIL: " + description + " is true");
            }
        } catch (Exception e) {
            config.logExceptionAndFail("Error asserting " + description, e);
        }
    }

    public static void assertElementVisible(Config config, Locator locator, String elementName) {
        try {
            config.logComment("Asserting " + elementName + " is visible");
            boolean isVisible = locator.isVisible();
            if (isVisible) {
                config.logPass("PASS: " + elementName + " is visible");
            } else {
                config.logFail("FAIL: " + elementName + " is not visible");
            }
        } catch (Exception e) {
            config.logExceptionAndFail("Error asserting visibility of " + elementName, e);
        }
    }
}
