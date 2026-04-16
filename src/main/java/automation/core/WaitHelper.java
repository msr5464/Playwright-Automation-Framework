package automation.core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;

public class WaitHelper {

    public static int getTimeout(Config config) {
        String objectWaitTime = config.getRunTimeProperty("ObjectWaitTime");
        int timeout = objectWaitTime != null ? Integer.parseInt(objectWaitTime) : 30;
        return timeout * 1000;
    }

    public static boolean waitForElementToBeVisible(Config config, Locator locator, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(getTimeout(config)));
            config.logCommentForDebugging("Element is visible: " + elementName);
            return true;
        } catch (Exception e) {
            config.logWarning("Element not visible after timeout: " + elementName);
            return false;
        }
    }

    public static boolean waitForOptionalElementToBeVisible(Config config, Locator locator, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(5000));
            config.logCommentForDebugging("Element is visible: " + elementName);
            return true;
        } catch (Exception e) {
            config.logCommentForDebugging("Optional element not visible after timeout: " + elementName);
            return false;
        }
    }

    public static boolean waitForElementToBeHidden(Config config, Locator locator, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(getTimeout(config)));
            config.logCommentForDebugging("Element is hidden: " + elementName);
            return true;
        } catch (Exception e) {
            config.logWarning("Element still visible after timeout: " + elementName);
            return false;
        }
    }

    public static boolean waitForElementToBeAttached(Config config, Locator locator, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(getTimeout(config)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean waitForElementToBeDetached(Config config, Locator locator, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.DETACHED)
                    .setTimeout(getTimeout(config)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean waitForAnyElementToBeDisplayed(Config config, Locator... locators) {
        long startTime = System.currentTimeMillis();
        int timeout = getTimeout(config);
        while (System.currentTimeMillis() - startTime < timeout) {
            for (Locator locator : locators) {
                try {
                    if (locator.isVisible())
                        return true;
                } catch (Exception ignored) {
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        return false;
    }

    public static void waitForNetworkIdle(Config config) {
        try {
            config.page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        } catch (Exception e) {
            config.logWarning("Network idle wait failed: " + e.getMessage());
        }
    }

    /**
     * Smart loading detection - waits for loading indicators to disappear.
     */
    public static void waitForLoadingComplete(Config config, Locator loadingIndicator) {
        try {
            if (loadingIndicator.isVisible()) {
                loadingIndicator.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(getTimeout(config)));
            }
        } catch (Exception e) {
            config.logWarning("Loading indicator wait: " + e.getMessage());
        }
    }

    public static void waitForPageLoad(Config config) {
        try {
            config.page.waitForLoadState();
        } catch (Exception e) {
            Log.debug(config, "Page load state wait failed: " + e.getMessage());
        }
    }
}
