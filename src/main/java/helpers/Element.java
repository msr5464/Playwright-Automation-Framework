package helpers;

import com.microsoft.playwright.Locator;

public class Element {

    public static String getCurrentPageName() {
        String callingClassName = null;
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Element.class.getName()) && !ste.getClassName().contains("Helper")
                    && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                callingClassName = ste.getClassName();
                return callingClassName.substring(callingClassName.lastIndexOf('.') + 1);
            }
        }
        return null;
    }

    /**
     * Handle multiple elements found with same locator by using the first element
     * Logs a warning if multiple elements are found
     */
    private static Locator handleMultipleElements(Config config, Locator locator, String elementName) {
        try {
            int count = locator.count();
            if (count > 1) {
                config.logWarning("Multiple elements (" + count + ") found for " + elementName + ". Using the first element.");
                return locator.first();
            }
            return locator;
        } catch (Exception e) {
            // If count() fails, return original locator
            config.logComment("Could not determine element count for " + elementName + ", using original locator");
            return locator;
        }
    }

    public static void click(Config config, Locator locator, String elementName) {
        try {
            // Handle multiple elements by using the first one
            Locator targetLocator = handleMultipleElements(config, locator, elementName);
            
            // Wait for element to be visible before clicking
            WaitHelper.waitForElementToBeVisible(config, targetLocator, elementName);
            config.logComment("Clicking on " + elementName + " present on '" + getCurrentPageName() + "'");
            targetLocator.click();
        } catch (Exception e) {
            config.logExceptionAndFail("Failed to click on " + elementName + " on '" + getCurrentPageName() + "'", e);
        }
    }

    public static void enterData(Config config, Locator locator, String data, String elementName) {
        try {
            // Handle multiple elements by using the first one
            Locator targetLocator = handleMultipleElements(config, locator, elementName);
            
            // Wait for element to be visible before entering data
            WaitHelper.waitForElementToBeVisible(config, targetLocator, elementName);
            config.logComment("Entering data as '" + data + "' in '" + elementName + "' field present on '"
                    + getCurrentPageName() + "'");
            targetLocator.clear();
            targetLocator.fill(data);
        } catch (Exception e) {
            config.logExceptionAndFail("Failed to enter data in " + elementName + " on '" + getCurrentPageName() + "'",
                    e);
        }
    }

    public static String getText(Config config, Locator locator, String elementName) {
        try {
            // Handle multiple elements by using the first one
            Locator targetLocator = handleMultipleElements(config, locator, elementName);
            
            // Wait for element to be visible before getting text
            WaitHelper.waitForElementToBeVisible(config, targetLocator, elementName);
            config.logComment("Getting text from " + elementName + " present on '" + getCurrentPageName() + "'");
            return targetLocator.textContent();
        } catch (Exception e) {
            config.logExceptionAndFail("Failed to get text from " + elementName + " on '" + getCurrentPageName() + "'",
                    e);
            return "";
        }
    }

    public static boolean isDisplayed(Config config, Locator locator, String elementName) {
        try {
            // Handle multiple elements by using the first one
            Locator targetLocator = handleMultipleElements(config, locator, elementName);
            
            // Wait for element to be visible before checking
            WaitHelper.waitForElementToBeVisible(config, targetLocator, elementName);
            config.logComment("Checking if " + elementName + " is displayed on '" + getCurrentPageName() + "'");
            return targetLocator.isVisible();
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to check visibility of " + elementName + " on '" + getCurrentPageName() + "'", e);
            return false;
        }
    }
}
