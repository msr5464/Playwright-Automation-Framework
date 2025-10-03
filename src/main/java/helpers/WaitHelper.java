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
     * Wait for page to load completely (DOM content loaded)
     * Use this after navigation to ensure page is fully loaded
     */
    public static void waitForPageLoad(Config config, Locator locator) {
        Long objectWaitTime = Long.parseLong(config.getRunTimeProperty("ObjectWaitTime"));
        try {
            long startTime = System.currentTimeMillis();
            config.logComment("Waiting for '" + Element.getCurrentPageName() + "' page to load...");
            config.page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            
            locator.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(objectWaitTime * 1000)); // Convert to milliseconds
                //calculate the time taken to load the page
                double timeTaken = (System.currentTimeMillis() - startTime) / 1000.0;
                config.logCommentForDebugging("'" + Element.getCurrentPageName() + "' page loaded after " + timeTaken + " seconds");
        } catch (Exception e) {
            config.logExceptionAndFail("Page did not load even after "+objectWaitTime+" seconds", e);
        }
    }
    
    /**
     * Wait for element to be visible
     * Use this before performing actions on elements
     */
    public static void waitForElementToBeVisible(Config config, Locator locator, String elementName) {
        
        Long objectWaitTime = Long.parseLong(config.getRunTimeProperty("ObjectWaitTime"));
        try {
            // Handle multiple elements by using the first one
            Locator targetLocator = handleMultipleElements(config, locator, elementName);
                
            config.logComment("Waiting for '" + elementName + "' to be visible");
            targetLocator.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(objectWaitTime * 1000)); // Convert to milliseconds
        } catch (Exception e) {
            config.logExceptionAndFail("Element " + elementName + " did not become visible even after "+objectWaitTime+" seconds", e);
        }
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
    
    /**
     * Wait for optional element to be visible with short timeout
     * Use this for optional UI elements that may or may not appear
     * Returns true if element becomes visible, false if timeout occurs
     */
    public static boolean waitForOptionalElementToBeVisible(Config config, Locator locator, String elementName, int timeoutSeconds) {
        try {
            // Handle multiple elements by using the first one
            Locator targetLocator = handleMultipleElements(config, locator, elementName);
            
            config.logComment("Checking for " + elementName + " (optional, timeout: " + timeoutSeconds + "s)...");
            targetLocator.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutSeconds * 1000));
            config.logComment(elementName + " detected");
            return true;
        } catch (Exception e) {
            config.logComment(elementName + " not found or already closed (this is normal)");
            return false;
        }
    }
    
    /**
     * Wait for URL to contain expected text
     * Use this when waiting for navigation to complete
     */
    public static void waitForUrl(Config config, String expectedUrl) {
        try {
            // Get timeout from configuration (ObjectWaitTime is in seconds)
            Long objectWaitTime = Long.parseLong(config.getRunTimeProperty("ObjectWaitTime"));
            
            config.logComment("Waiting for URL to contain: " + expectedUrl);
            config.page.waitForURL(url -> url.contains(expectedUrl), 
                new com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(objectWaitTime * 1000)); // Convert to milliseconds
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
            // Handle multiple elements by using the first one
            Locator targetLocator = handleMultipleElements(config, locator, elementName);
            
            // Get timeout from configuration (ObjectWaitTime is in seconds)
            Long objectWaitTime = Long.parseLong(config.getRunTimeProperty("ObjectWaitTime"));
            
            config.logComment("Waiting for " + elementName + " to disappear");
            targetLocator.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
                .setTimeout(objectWaitTime * 1000)); // Convert to milliseconds
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
            // Get timeout from configuration (ObjectWaitTime is in seconds)
            Long objectWaitTime = Long.parseLong(config.getRunTimeProperty("ObjectWaitTime"));
            
            config.logComment("Waiting for text to appear: " + text);
            config.page.waitForSelector("text=" + text, 
                new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(objectWaitTime * 1000)); // Convert to milliseconds
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