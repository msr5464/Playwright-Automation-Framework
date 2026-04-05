package automation.core;

import com.microsoft.playwright.Locator;

/**
 * Element interaction wrapper providing consistent logging, waiting, and error handling.
 * Wraps Playwright Locator with auto-logging and smart waits.
 */
public class Element
{

    // ========== CLICK ==========

    public static void click(Config config, Locator locator, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Clicking: " + elementName);
        try
        {
            locator.scrollIntoViewIfNeeded();
            locator.click();
        }
        catch (Exception e)
        {
            Log.warning(config, "Standard click failed for '" + elementName + "', trying JS click");
            clickThroughJS(config, locator, elementName);
        }
    }

    public static void clickThroughJS(Config config, Locator locator, String elementName)
    {
        Log.action(config, "JS clicking: " + elementName);
        locator.evaluate("el => el.click()");
    }

    public static void clickViaCoordinates(Config config, Locator locator, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Coordinate clicking: " + elementName);
        locator.click(new Locator.ClickOptions().setPosition(0, 0));
    }

    public static void doubleClick(Config config, Locator locator, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Double clicking: " + elementName);
        locator.dblclick();
    }

    // ========== TEXT INPUT ==========

    public static void enterData(Config config, Locator locator, String text, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Entering in '" + elementName + "': " + text);
        locator.clear();
        locator.fill(text);
    }

    public static void clearAndType(Config config, Locator locator, String text, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Clearing and typing in '" + elementName + "': " + text);
        locator.clear();
        locator.pressSequentially(text);
    }

    public static void appendText(Config config, Locator locator, String text, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Appending to '" + elementName + "': " + text);
        locator.pressSequentially(text);
    }

    // ========== CHECKBOX ==========

    public static void check(Config config, Locator locator, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        if (!locator.isChecked())
        {
            Log.action(config, "Checking: " + elementName);
            locator.check();
        }
    }

    public static void uncheck(Config config, Locator locator, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        if (locator.isChecked())
        {
            Log.action(config, "Unchecking: " + elementName);
            locator.uncheck();
        }
    }

    // ========== TEXT RETRIEVAL ==========

    public static String getText(Config config, Locator locator, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        String text = locator.textContent();
        Log.debug(config, "Text from '" + elementName + "': " + text);
        return text != null ? text.trim() : "";
    }

    public static String getInputValue(Config config, Locator locator, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        return locator.inputValue();
    }

    public static String getAttribute(Config config, Locator locator, String attribute, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        return locator.getAttribute(attribute);
    }

    // ========== ELEMENT STATE ==========

    public static boolean isElementDisplayed(Config config, Locator locator, String elementName)
    {
        try
        {
            return locator.isVisible();
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static boolean isElementEnabled(Config config, Locator locator, String elementName)
    {
        try
        {
            return locator.isEnabled();
        }
        catch (Exception e)
        {
            return false;
        }
    }

    // ========== SELECT / DROPDOWN ==========

    public static void selectOption(Config config, Locator locator, String value, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Selecting '" + value + "' in: " + elementName);
        locator.selectOption(value);
    }

    // ========== FILE UPLOAD ==========

    public static void uploadFile(Config config, Locator locator, String filePath, String elementName)
    {
        Log.action(config, "Uploading to '" + elementName + "': " + filePath);
        locator.setInputFiles(java.nio.file.Paths.get(filePath));
    }

    public static boolean isElementChecked(Config config, Locator locator, String elementName)
    {
        try
        {
            return locator.isChecked();
        }
        catch (Exception e)
        {
            return false;
        }
    }

    // ========== SCROLL ==========

    public static void scrollToElement(Config config, Locator locator, String elementName)
    {
        Log.action(config, "Scrolling to: " + elementName);
        locator.scrollIntoViewIfNeeded();
    }

    // ========== HOVER ==========

    public static void hover(Config config, Locator locator, String elementName)
    {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Hovering over: " + elementName);
        locator.hover();
    }

    // ========== COUNT ==========

    public static int getCount(Config config, Locator locator, String elementName)
    {
        int count = locator.count();
        Log.debug(config, elementName + " count: " + count);
        return count;
    }
}
