package automation.core;

import com.microsoft.playwright.Locator;

/**
 * Element interaction wrapper providing consistent logging, waiting, and error
 * handling.
 * Wraps Playwright Locator with auto-logging and smart waits.
 */
public class Element {

    /**
     * The budget for the action that follows a visibility wait.
     *
     * <p>The wait already spent the full ObjectWaitTime on this element. When it did
     * not succeed, acting with a budget of its own spends that time a second time
     * for the same answer and the same error — a minute per missing element instead
     * of half of one. So a failed wait leaves the action 1ms: it fails at once, with
     * the wording and the Playwright error every caller already expects.
     */
    private static double actionTimeout(Config config, boolean visible) {
        return visible ? WaitHelper.getTimeout(config) : 1;
    }

    // ========== CLICK ==========

    public static void click(Config config, Locator locator, String elementName) {
        // The wait already spent the full ObjectWaitTime on this element; clicking
        // with a budget would spend it again for the same answer and the same error.
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        if (visible) {
            Log.action(config, "Clicking: " + elementName);
        } else {
            // Say so: the 1ms timeout in the error below otherwise reads as a misconfig.
            Log.action(config, "Clicking: " + elementName
                    + " (the wait did not succeed — failing fast rather than "
                    + "waiting a second time)");
        }
        try {
            if (visible) {
                locator.scrollIntoViewIfNeeded();
                locator.click();
            } else {
                // Scroll skipped too — it carries its own full timeout.
                locator.click(new Locator.ClickOptions().setTimeout(1));
            }
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
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Coordinate clicking: " + elementName);
        try {
            locator.click(new Locator.ClickOptions().setPosition(0, 0)
                    .setTimeout(actionTimeout(config, visible)));
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to coordinate-click on element '" + elementName + "' with locator: " + locator.toString(),
                    e);
        }
    }

    public static void doubleClick(Config config, Locator locator, String elementName) {
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Double clicking: " + elementName);
        try {
            locator.dblclick(new Locator.DblclickOptions()
                    .setTimeout(actionTimeout(config, visible)));
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to double-click on element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    // ========== TEXT INPUT ==========

    public static void enterData(Config config, Locator locator, String text, String elementName) {
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Entering in '" + elementName + "': " + text);
        try {
            double timeout = actionTimeout(config, visible);
            locator.clear(new Locator.ClearOptions().setTimeout(timeout));
            locator.fill(text, new Locator.FillOptions().setTimeout(timeout));
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to enter data in element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    public static void clearAndType(Config config, Locator locator, String text, String elementName) {
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Clearing and typing in '" + elementName + "': " + text);
        try {
            double timeout = actionTimeout(config, visible);
            locator.clear(new Locator.ClearOptions().setTimeout(timeout));
            locator.pressSequentially(text,
                    new Locator.PressSequentiallyOptions().setTimeout(timeout));
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to clear and type in element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    public static void appendText(Config config, Locator locator, String text, String elementName) {
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Appending to '" + elementName + "': " + text);
        try {
            locator.pressSequentially(text, new Locator.PressSequentiallyOptions()
                    .setTimeout(actionTimeout(config, visible)));
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to append text to element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    // ========== CHECKBOX ==========

    public static void check(Config config, Locator locator, String elementName) {
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        try {
            double timeout = actionTimeout(config, visible);
            if (!locator.isChecked(new Locator.IsCheckedOptions().setTimeout(timeout))) {
                Log.action(config, "Checking: " + elementName);
                locator.check(new Locator.CheckOptions().setTimeout(timeout));
            }
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to check element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    public static void uncheck(Config config, Locator locator, String elementName) {
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        try {
            double timeout = actionTimeout(config, visible);
            if (locator.isChecked(new Locator.IsCheckedOptions().setTimeout(timeout))) {
                Log.action(config, "Unchecking: " + elementName);
                locator.uncheck(new Locator.UncheckOptions().setTimeout(timeout));
            }
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to uncheck element '" + elementName + "' with locator: " + locator.toString(), e);
        }
    }

    // ========== TEXT RETRIEVAL ==========

    public static String getText(Config config, Locator locator, String elementName) {
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        try {
            String text = locator.textContent(new Locator.TextContentOptions()
                    .setTimeout(actionTimeout(config, visible)));
            Log.debug(config, "Text from '" + elementName + "': " + text);
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to get text from element '" + elementName + "' with locator: " + locator.toString(), e);
            return "";
        }
    }

    public static String getInputValue(Config config, Locator locator, String elementName) {
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        try {
            return locator.inputValue(new Locator.InputValueOptions()
                    .setTimeout(actionTimeout(config, visible)));
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to get input value from element '" + elementName + "' with locator: " + locator.toString(),
                    e);
            return "";
        }
    }

    public static String getAttribute(Config config, Locator locator, String attribute, String elementName) {
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        try {
            return locator.getAttribute(attribute, new Locator.GetAttributeOptions()
                    .setTimeout(actionTimeout(config, visible)));
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
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Selecting '" + value + "' in: " + elementName);
        try {
            locator.selectOption(value, new Locator.SelectOptionOptions()
                    .setTimeout(actionTimeout(config, visible)));
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
        boolean visible = WaitHelper.waitForElementToBeVisible(config, locator, elementName);
        Log.action(config, "Hovering over: " + elementName);
        try {
            locator.hover(new Locator.HoverOptions()
                    .setTimeout(actionTimeout(config, visible)));
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
