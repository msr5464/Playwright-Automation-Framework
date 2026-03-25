package automation.core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import automation.core.Config;

import org.testng.Assert;
import org.testng.asserts.SoftAssert;

public class AssertHelper
{

    // ========== HARD ASSERTIONS ==========

    public static void assertEquals(Config config, String actual, String expected, String message)
    {
        if (actual != null && actual.equals(expected))
        {
            Log.pass(config, message + " | Expected: '" + expected + "' | Actual: '" + actual + "'");
        }
        else
        {
            Log.fail(config, message + " | Expected: '" + expected + "' | Actual: '" + actual + "'");
            Assert.assertEquals(actual, expected, message);
        }
    }

    public static void assertEquals(Config config, int actual, int expected, String message)
    {
        if (actual == expected)
        {
            Log.pass(config, message + " | Expected: " + expected + " | Actual: " + actual);
        }
        else
        {
            Log.fail(config, message + " | Expected: " + expected + " | Actual: " + actual);
            Assert.assertEquals(actual, expected, message);
        }
    }

    public static void assertTrue(Config config, boolean condition, String message)
    {
        if (condition)
        {
            Log.pass(config, message);
        }
        else
        {
            Log.fail(config, message);
            Assert.assertTrue(false, message);
        }
    }

    public static void assertFalse(Config config, boolean condition, String message)
    {
        if (!condition)
        {
            Log.pass(config, message);
        }
        else
        {
            Log.fail(config, message);
            Assert.assertFalse(true, message);
        }
    }

    public static void assertContains(Config config, String actual, String expected, String message)
    {
        if (actual != null && actual.contains(expected))
        {
            Log.pass(config, message + " | '" + actual + "' contains '" + expected + "'");
        }
        else
        {
            Log.fail(config, message + " | '" + actual + "' does not contain '" + expected + "'");
            Assert.fail(message + " | '" + actual + "' does not contain '" + expected + "'");
        }
    }

    public static void assertNotNull(Config config, Object object, String message)
    {
        if (object != null)
        {
            Log.pass(config, message + " | Object is not null");
        }
        else
        {
            Log.fail(config, message + " | Object is null");
            Assert.assertNotNull(object, message);
        }
    }

    // ========== ELEMENT ASSERTIONS ==========

    public static void assertElementVisible(Config config, Locator locator, String elementName)
    {
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        if (visible)
        {
            Log.pass(config, "Element visible: " + elementName);
        }
        else
        {
            Log.fail(config, "Element NOT visible: " + elementName);
            Assert.fail("Element NOT visible: " + elementName);
        }
    }

    public static void assertElementHidden(Config config, Locator locator, String elementName)
    {
        boolean hidden = WaitHelper.waitForElementToBeHidden(config, locator, elementName);
        if (hidden)
        {
            Log.pass(config, "Element hidden: " + elementName);
        }
        else
        {
            Log.fail(config, "Element NOT hidden: " + elementName);
            Assert.fail("Element NOT hidden: " + elementName);
        }
    }

    public static void assertElementText(Config config, Locator locator, String expectedText, String elementName)
    {
        try
        {
            String actualText = locator.textContent();
            if (actualText != null && actualText.trim().contains(expectedText))
            {
                Log.pass(config, elementName + " text matches: '" + expectedText + "'");
            }
            else
            {
                Log.fail(config, elementName + " text mismatch | Expected: '" + expectedText + "' | Actual: '" + actualText + "'");
                Assert.fail(elementName + " text mismatch | Expected: '" + expectedText + "' | Actual: '" + actualText + "'");
            }
        }
        catch (Exception e)
        {
            Log.fail(config, "Failed to get text from " + elementName + ": " + e.getMessage());
            Assert.fail("Failed to get text from " + elementName);
        }
    }

    // ========== SOFT ASSERTIONS ==========

    public static void softAssertEquals(Config config, String actual, String expected, String message)
    {
        if (actual != null && actual.equals(expected))
        {
            Log.pass(config, "[Soft] " + message);
        }
        else
        {
            Log.warning(config, "[Soft Fail] " + message + " | Expected: '" + expected + "' | Actual: '" + actual + "'");
            config.softAssert.assertEquals(actual, expected, message);
        }
    }

    public static void softAssertTrue(Config config, boolean condition, String message)
    {
        if (condition)
        {
            Log.pass(config, "[Soft] " + message);
        }
        else
        {
            Log.warning(config, "[Soft Fail] " + message);
            config.softAssert.assertTrue(condition, message);
        }
    }

    public static void softAssertContains(Config config, String actual, String expected, String message)
    {
        if (actual != null && actual.contains(expected))
        {
            Log.pass(config, "[Soft] " + message);
        }
        else
        {
            Log.warning(config, "[Soft Fail] " + message + " | '" + actual + "' does not contain '" + expected + "'");
            config.softAssert.fail(message + " | '" + actual + "' does not contain '" + expected + "'");
        }
    }

    /**
     * Call this at the end of a test to assert all soft assertions.
     */
    public static void assertAll(Config config)
    {
        try
        {
            config.softAssert.assertAll();
            Log.pass(config, "All soft assertions passed");
        }
        catch (AssertionError e)
        {
            Log.fail(config, "Soft assertions failed: " + e.getMessage());
            throw e;
        }
    }
}
