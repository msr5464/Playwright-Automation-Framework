package automation.core;

import com.microsoft.playwright.Locator;

/**
 * Element interaction wrapper providing consistent logging, waiting, and error
 * handling.
 * Wraps Playwright Locator with auto-logging and smart waits.
 */
public class Element {

    // ========== CLICK ==========

    public static void click(Config config, Locator locator, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Clicking: " + elementName);
        try {
            locator.scrollIntoViewIfNeeded();
            locator.click();
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to click on element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    public static void clickThroughJS(Config config, Locator locator, String elementName) {

        try {
            Log.action(config, "JS clicking: " + elementName);
            locator.evaluate("el => el.click()");
        } catch (Exception e) {
            config.logExceptionAndFail("JS clicking failed for '" + elementName + "'!", e);
        }
    }

    public static void clickViaCoordinates(Config config, Locator locator, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Coordinate clicking: " + elementName);
        try {
            locator.click(new Locator.ClickOptions().setPosition(0, 0));
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to coordinate-click on element '" + elementName + "' with locator: " + locator.toString(),
                    e);
        }
    }

    public static void doubleClick(Config config, Locator locator, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Double clicking: " + elementName);
        try {
            locator.dblclick();
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to double-click on element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    // ========== TEXT INPUT ==========

    public static void enterData(Config config, Locator locator, String text, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Entering in '" + elementName + "': " + text);
        try {
            locator.clear();
            locator.fill(text);
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to enter data in element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    public static void clearAndType(Config config, Locator locator, String text, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Clearing and typing in '" + elementName + "': " + text);
        try {
            locator.clear();
            locator.pressSequentially(text);
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to clear and type in element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    public static void appendText(Config config, Locator locator, String text, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Appending to '" + elementName + "': " + text);
        try {
            locator.pressSequentially(text);
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to append text to element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    // ========== CHECKBOX ==========

    public static void check(Config config, Locator locator, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        try {
            if (!locator.isChecked()) {
                Log.action(config, "Checking: " + elementName);
                locator.check();
            }
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to check element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    public static void uncheck(Config config, Locator locator, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        try {
            if (locator.isChecked()) {
                Log.action(config, "Unchecking: " + elementName);
                locator.uncheck();
            }
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to uncheck element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    // ========== TEXT RETRIEVAL ==========

    public static String getText(Config config, Locator locator, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        try {
            String text = locator.textContent();
            Log.debug(config, "Text from '" + elementName + "': " + text);
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to get text from element '" + elementName + "' with locator: " + locator.toString(), e);
            return "";
        }
    }

    public static String getInputValue(Config config, Locator locator, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        try {
            return locator.inputValue();
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to get input value from element '" + elementName + "' with locator: " + locator.toString(),
                    e);
            return "";
        }
    }

    public static String getAttribute(Config config, Locator locator, String attribute, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        try {
            return locator.getAttribute(attribute);
        } catch (Exception e) {
            config.logExceptionAndFail("Failed to get attribute '" + attribute + "' from element '" + elementName
                    + "' with locator: " + locator.toString(), e);
            return "";
        }
    }

    // ========== ELEMENT STATE ==========

    public static boolean isElementDisplayed(Config config, Locator locator, String elementName) {
        try {
            return locator.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isElementEnabled(Config config, Locator locator, String elementName) {
        try {
            return locator.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    // ========== SELECT / DROPDOWN ==========

    public static void selectOption(Config config, Locator locator, String value, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Selecting '" + value + "' in: " + elementName);
        try {
            locator.selectOption(value);
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to select option '" + value + "' in element '" + elementName + "' with locator: "
                            + locator.toString(),
                    e);
        }
    }

    // ========== FILE UPLOAD ==========

    public static void uploadFile(Config config, Locator locator, String filePath, String elementName) {
        Log.action(config, "Uploading to '" + elementName + "': " + filePath);
        try {
            locator.setInputFiles(java.nio.file.Paths.get(filePath));
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to upload file '" + filePath + "' to element '" + elementName + "' with locator: "
                            + locator.toString(),
                    e);
        }
    }

    public static boolean isElementChecked(Config config, Locator locator, String elementName) {
        try {
            return locator.isChecked();
        } catch (Exception e) {
            return false;
        }
    }

    // ========== SCROLL ==========

    public static void scrollToElement(Config config, Locator locator, String elementName) {
        Log.action(config, "Scrolling to: " + elementName);
        try {
            locator.scrollIntoViewIfNeeded();
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to scroll to element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    // ========== HOVER ==========

    public static void hover(Config config, Locator locator, String elementName) {
        WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Hovering over: " + elementName);
        try {
            locator.hover();
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to hover over element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    // ========== COUNT ==========

    public static int getCount(Config config, Locator locator, String elementName) {
        try {
            int count = locator.count();
            Log.debug(config, elementName + " count: " + count);
            return count;
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to get count for element '" + elementName + "' with locator: " + locator.toString(), e);
            return 0;
        }
    }
}
