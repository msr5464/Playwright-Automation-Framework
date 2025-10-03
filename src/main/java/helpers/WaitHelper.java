package helpers;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;

/**
 * WaitHelper provides wait utilities for scenarios where Playwright's auto-wait doesn't apply.
 * 
 * Note: Playwright automatically waits for elements during actions (click, fill, etc.).
 * Use this helper only for:
 * - Page navigation waits
 * - URL changes
 * - Custom conditions
 * - Elements to disappear
 */
public class WaitHelper {

    /**
     * Wait for page to load completely (network idle)
     * Use this after navigation to ensure page is fully loaded
     */
    public static void waitForPageLoad(Config config) {
        try {
            config.logComment("Waiting for page to load completely");
            config.page.waitForLoadState(LoadState.NETWORKIDLE);
        } catch (Exception e) {
            config.logExceptionAndFail("Page did not load within timeout", e);
        }
    }
    
    /**
     * Wait for URL to contain expected text
     * Use this when waiting for navigation to complete
     */
    public static void waitForUrl(Config config, String expectedUrl) {
        try {
            config.logComment("Waiting for URL to contain: " + expectedUrl);
            config.page.waitForURL(url -> url.contains(expectedUrl), 
                new com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(30000));
        } catch (Exception e) {
            config.logExceptionAndFail("URL did not change to expected value: " + expectedUrl, e);
        }
    }
    
    /**
     * Wait for element to disappear (useful for loading spinners, etc.)
     * Playwright's auto-wait doesn't handle this scenario
     */
    public static void waitForElementToDisappear(Config config, Locator locator, String elementName) {
        try {
            config.logComment("Waiting for " + elementName + " to disappear");
            locator.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
                .setTimeout(30000));
        } catch (Exception e) {
            config.logExceptionAndFail("Element " + elementName + " did not disappear within timeout", e);
        }
    }
    
    /**
     * Wait for specific text to appear on page
     * Use this for custom text-based conditions
     */
    public static void waitForText(Config config, String text) {
        try {
            config.logComment("Waiting for text to appear: " + text);
            config.page.waitForSelector("text=" + text, 
                new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(30000));
        } catch (Exception e) {
            config.logExceptionAndFail("Text '" + text + "' did not appear within timeout", e);
        }
    }
    
    public static void waitforseconds(Config config, int seconds) {
        try {
            config.logComment("Waiting for " + seconds + " seconds");
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            config.logExceptionAndFail("Failed to wait for " + seconds + " seconds", e);
        }
    }
}