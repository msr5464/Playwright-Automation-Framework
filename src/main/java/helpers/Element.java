package helpers;

import com.microsoft.playwright.Locator;

public class Element {

    private static String getCurrentPageName() {
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

    public static void click(Config config, Locator locator, String elementName) {
        try {
            config.logComment("Clicking on " + elementName + " present on '" + getCurrentPageName() + "'");
            locator.click();
        } catch (Exception e) {
            config.logExceptionAndFail("Failed to click on " + elementName + " on '" + getCurrentPageName() + "'", e);
        }
    }

    public static void enterData(Config config, Locator locator, String data, String elementName) {
        try {
            config.logComment("Entering data as '" + data + "' in '" + elementName + "' field present on '"
                    + getCurrentPageName() + "'");
            locator.clear();
            locator.fill(data);
        } catch (Exception e) {
            config.logExceptionAndFail("Failed to enter data in " + elementName + " on '" + getCurrentPageName() + "'",
                    e);
        }
    }

    public static String getText(Config config, Locator locator, String elementName) {
        try {
            config.logComment("Getting text from " + elementName + " present on '" + getCurrentPageName() + "'");
            return locator.textContent();
        } catch (Exception e) {
            config.logExceptionAndFail("Failed to get text from " + elementName + " on '" + getCurrentPageName() + "'",
                    e);
            return "";
        }
    }

    public static boolean isDisplayed(Config config, Locator locator, String elementName) {
        try {
            config.logComment("Checking if " + elementName + " is displayed on '" + getCurrentPageName() + "'");
            return locator.isVisible();
        } catch (Exception e) {
            config.logExceptionAndFail(
                    "Failed to check visibility of " + elementName + " on '" + getCurrentPageName() + "'", e);
            return false;
        }
    }
}
