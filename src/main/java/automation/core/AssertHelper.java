package automation.core;

import com.microsoft.playwright.Locator;

public class AssertHelper {

    // ========== ASSERTIONS (soft — execution continues, test fails at end) ==========

    public static void assertEquals(Config config, String actual, String expected, String message) {
        if (actual != null && actual.equals(expected)) {
            String msg = message.contains("should be") ? message.replace("should be", "is") : message + " is " + actual;
            Log.pass(config, "✔ PASS: Verified " + msg);
        } else {
            Log.fail(config, "✘ FAIL: " + message + " | Expected: '" + expected + "' | Actual: '" + actual + "'");
        }
    }

    public static void assertEquals(Config config, int actual, int expected, String message) {
        if (actual == expected) {
            String msg = message.contains("should be") ? message.replace("should be", "is") : message + " is " + actual;
            Log.pass(config, "✔ PASS: Verified " + msg);
        } else {
            Log.fail(config, "✘ FAIL: " + message + " | Expected: " + expected + " | Actual: " + actual);
        }
    }

    public static void assertTrue(Config config, boolean condition, String message) {
        if (condition) {
            Log.pass(config, "✔ PASS: " + message);
        } else {
            Log.fail(config, "✘ FAIL: " + message);
        }
    }

    public static void assertFalse(Config config, boolean condition, String message) {
        if (!condition) {
            Log.pass(config, "✔ PASS: " + message);
        } else {
            Log.fail(config, "✘ FAIL: " + message);
        }
    }

    public static void assertContains(Config config, String actual, String expected, String message) {
        if (actual != null && actual.contains(expected)) {
            String msg = message.contains("should be") ? message.replace("should be", "is")
                    : message + " contains " + expected;
            Log.pass(config, "✔ PASS: Verified " + msg);
        } else {
            Log.fail(config, "✘ FAIL: " + message + " | '" + actual + "' does not contain '" + expected + "'");
        }
    }

    public static void assertNotNull(Config config, Object object, String message) {
        if (object != null) {
            String msg = message.contains("should be") ? message.replace("should be", "is") : message;
            Log.pass(config, "✔ PASS: Verified " + msg);
        } else {
            Log.fail(config, "✘ FAIL: " + message + " | Object is null");
        }
    }

    public static void assertNull(Config config, Object object, String message) {
        if (object == null) {
            Log.pass(config, "✔ PASS: " + message + " | Object is null as expected");
        } else {
            Log.fail(config, "✘ FAIL: " + message + " | Expected null but got: " + object);
        }
    }

    // ========== ELEMENT ASSERTIONS ==========

    public static void assertElementVisible(Config config, Locator locator, String elementName) {
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        if (visible) {
            Log.pass(config, "✔ PASS: Element visible: " + elementName);
        } else {
            Log.fail(config, "✘ FAIL: Element NOT visible: " + elementName);
        }
    }

    public static void assertElementHidden(Config config, Locator locator, String elementName) {
        boolean hidden = WaitHelper.waitForElementToBeHidden(config, locator, elementName);
        if (hidden) {
            Log.pass(config, "✔ PASS: Element hidden: " + elementName);
        } else {
            Log.fail(config, "✘ FAIL: Element NOT hidden: " + elementName);
        }
    }

    public static void assertElementText(Config config, Locator locator, String expectedText, String elementName) {
        try {
            String actualText = locator.textContent();
            if (actualText != null && actualText.trim().contains(expectedText)) {
                Log.pass(config, "✔ PASS: " + elementName + " text matches: '" + expectedText + "'");
            } else {
                Log.fail(config, "✘ FAIL: " + elementName + " text mismatch | Expected: '" + expectedText
                        + "' | Actual: '" + actualText + "'");
            }
        } catch (Exception e) {
            Log.fail(config, "✘ FAIL: Failed to get text from " + elementName + ": " + e.getMessage());
        }
    }

    /**
     * Compare two string values and log pass/fail. Used by ApiHelper for JSON field verification.
     */
    public static void compareEquals(Config config, String what, String expected, String actual) {
        if (actual != null && actual.equals(expected)) {
            Log.pass(config, "✔ PASS: Verified " + what + " is '" + actual + "'");
        } else {
            Log.fail(config, "✘ FAIL: " + what + " | Expected: '" + expected + "' | Actual: '" + actual + "'");
        }
    }

    // ========== SOFT ASSERTIONS (same behavior — kept for backward compatibility) ==========

    public static void softAssertEquals(Config config, String actual, String expected, String message) {
        assertEquals(config, actual, expected, message);
    }

    public static void softAssertTrue(Config config, boolean condition, String message) {
        assertTrue(config, condition, message);
    }

    public static void softAssertContains(Config config, String actual, String expected, String message) {
        assertContains(config, actual, expected, message);
    }

    /**
     * Manually flush all recorded soft failures. Normally not needed — afterInvocation
     * in TestListener does this automatically. Use only when you want an early explicit check.
     */
    public static void assertAll(Config config) {
        config.softAssert.assertAll();
    }
}
