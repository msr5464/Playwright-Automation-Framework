package automation.core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.io.File;

/**
 * Base page object with common UI interaction patterns.
 * All page objects should extend this class.
 *
 * Low-level element interactions (click, fill, getText, etc.) delegate to
 * {@link Element} to avoid duplication. This class adds page-level concerns:
 * common locators, smart helpers (OTP, loading, overlay dismiss), navigation,
 * scroll, and keyboard shortcuts.
 */
public class BasePage
{

    protected final Config config;
    protected final Page page;

    // Common locators
    protected Locator loadingBar;
    protected Locator progressBar;
    protected Locator errorMessage;
    protected Locator searchField;

    public BasePage(Config config)
    {
        this.config = config;
        this.page = config.page;
        initCommonLocators();
        // So a later failure can name the page object that declares the locator that
        // failed. See Config.pageObjects.
        if (config != null)
        {
            config.pageObjects.add(this);
        }
    }

    /**
     * Assert that a page has loaded by checking that at least one of the given locators is visible.
     * Fails the test immediately if none of the elements appear within the configured timeout.
     *
     * Single element:
     *   assertPageLoaded(submitButton);
     *
     * Multiple elements — page is considered loaded if ANY one is visible:
     *   assertPageLoaded(avatarWidget, welcomeBanner);
     */
    protected void assertPageLoaded(Locator... locators)
    {
        boolean loaded = WaitHelper.waitForAnyElementToBeDisplayed(config, locators);
        if (loaded)
        {
            // Remember what this page looks like when it is right, so a later failure
            // can be diffed against it instead of guessed at.
            Baseline.record(config, this);
        }
        if (!loaded)
        {
            String locatorList = java.util.Arrays.stream(locators)
                .map(Locator::toString)
                .collect(java.util.stream.Collectors.joining(", "));
            // Record why, alongside the message rather than instead of it. This
            // assertion is really a page-identity check, but it fails with the wording
            // of an element lookup, so everything downstream reads a session that
            // expired or a navigation that never happened as a stale locator. The
            // message stays as it is — several consumers key off it — and the evidence
            // that tells those cases apart goes into a file next to the DOM snapshot.
            FailureContext.write(config, this, java.util.Arrays.asList(locators),
                WaitHelper.LAST_WAIT_MS.get(),
                WaitHelper.getTimeout(config),
                WaitHelper.LAST_WAIT_DOM_CHANGED.get());

            config.logFailToEndExecution("Failed to load Element " + locatorList + " in " + this.getClass().getSimpleName());
        }
    }

    protected void initCommonLocators()
    {
        this.loadingBar = page.locator("[data-cy='loading-bar'], .loading-indicator, .spinner");
        this.progressBar = page.locator("[role='progressbar'], .progress-bar");
        this.errorMessage = page.locator("[data-cy='error-message'], .error-message");
        this.searchField = page.locator("[data-cy='search-field'], input[type='search']");
    }

    // ========== ELEMENT INTERACTIONS (delegating to Element utility) ==========

    public void click(Locator locator, String elementName)
    {
        Element.click(config, locator, elementName);
    }

    public void clickForce(Locator locator, String elementName)
    {
        Log.action(config, "Force clicking: " + elementName);
        locator.click(new Locator.ClickOptions().setForce(true));
    }

    public void clickViaJS(Locator locator, String elementName)
    {
        Element.clickThroughJS(config, locator, elementName);
    }

    public void doubleClick(Locator locator, String elementName)
    {
        Element.doubleClick(config, locator, elementName);
    }

    public void fillText(Locator locator, String text, String elementName)
    {
        Element.enterData(config, locator, text, elementName);
    }

    public void typeText(Locator locator, String text, String elementName)
    {
        Element.clearAndType(config, locator, text, elementName);
    }

    public void selectOption(Locator dropdown, String value, String elementName)
    {
        Element.selectOption(config, dropdown, value, elementName);
    }

    public void selectOptionBySearch(Locator searchInput, String searchText, Locator optionToClick,
                                      String elementName)
    {
        Log.action(config, "Search-selecting '" + searchText + "' in: " + elementName);
        fillText(searchInput, searchText, elementName + " search");
        click(optionToClick, elementName + " option");
    }

    public void check(Locator locator, String elementName)
    {
        Element.check(config, locator, elementName);
    }

    public void uncheck(Locator locator, String elementName)
    {
        Element.uncheck(config, locator, elementName);
    }

    public String getText(Locator locator, String elementName)
    {
        return Element.getText(config, locator, elementName);
    }

    public String getInputValue(Locator locator, String elementName)
    {
        return Element.getInputValue(config, locator, elementName);
    }

    public String getAttribute(Locator locator, String attribute, String elementName)
    {
        return Element.getAttribute(config, locator, attribute, elementName);
    }

    public boolean isElementDisplayed(Locator locator)
    {
        return Element.isElementDisplayed(config, locator, "");
    }

    public boolean isElementEnabled(Locator locator)
    {
        return Element.isElementEnabled(config, locator, "");
    }

    public boolean isElementChecked(Locator locator)
    {
        return Element.isElementChecked(config, locator, "");
    }

    public void hover(Locator locator, String elementName)
    {
        Element.hover(config, locator, elementName);
    }

    // ========== FILE UPLOAD ==========

    public void uploadFile(Locator fileInput, String fileName, String elementName)
    {
        String absolutePath = resolveFilePath(fileName);
        Element.uploadFile(config, fileInput, absolutePath, elementName);
    }

    protected String resolveFilePath(String fileName)
    {
        String testResources = Config.testResourcesPath + fileName;
        if (new File(testResources).exists()) return testResources;
        String projectRoot = System.getProperty("user.dir") + File.separator + fileName;
        if (new File(projectRoot).exists()) return projectRoot;
        return fileName;
    }

    // ========== SMART HELPERS (page-level, not in Element) ==========

    /**
     * Click until the next element becomes visible.
     * Useful for buttons that may need multiple clicks due to animations or loading.
     */
    public void clickUntilNextElementIsLoaded(Locator clickTarget, Locator nextElement,
                                               String clickName, String nextName, int maxRetries)
    {
        for (int i = 0; i < maxRetries; i++)
        {
            try
            {
                click(clickTarget, clickName);
                if (WaitHelper.waitForOptionalElementToBeVisible(config, nextElement, nextName))
                {
                    Log.debug(config, nextName + " appeared after " + (i + 1) + " click(s)");
                    return;
                }
                Thread.sleep(2000);
            }
            catch (Exception e)
            {
                Log.debug(config, "Retry click " + (i + 1) + "/" + maxRetries + ": " + e.getMessage());
            }
        }
        Log.warning(config, nextName + " not visible after " + maxRetries + " clicks on " + clickName);
    }

    public void clickUntilNextElementIsLoaded(Locator clickTarget, Locator nextElement,
                                               String clickName, String nextName)
    {
        clickUntilNextElementIsLoaded(clickTarget, nextElement, clickName, nextName, 5);
    }

    /**
     * Wait for all loading indicators to disappear.
     */
    public void waitForLoadingComplete()
    {
        WaitHelper.waitForLoadingComplete(config, loadingBar);
        WaitHelper.waitForLoadingComplete(config, progressBar);
    }

    /**
     * Dismiss overlay tooltips if visible.
     */
    public void dismissOverlayIfVisible(Locator overlay)
    {
        try
        {
            if (overlay.isVisible())
            {
                overlay.click();
                Log.debug(config, "Dismissed overlay/tooltip");
            }
        }
        catch (Exception e)
        {
            // Overlay not present, continue
        }
    }

    /**
     * Input OTP digits across multiple fields with retry logic.
     */
    public void inputOTP(String otp, Locator otpContainer, int maxRetries)
    {
        Log.action(config, "Entering OTP: " + otp);
        for (int attempt = 0; attempt < maxRetries; attempt++)
        {
            try
            {
                Locator otpFields = otpContainer.locator("input");
                int fieldCount = otpFields.count();

                if (fieldCount > 0 && fieldCount >= otp.length())
                {
                    for (int i = 0; i < otp.length(); i++)
                    {
                        otpFields.nth(i).fill(String.valueOf(otp.charAt(i)));
                    }
                }
                else
                {
                    otpContainer.fill(otp);
                }

                Thread.sleep(1000);
                if (!isElementDisplayed(page.locator("[data-cy='otp-error'], .otp-error")))
                {
                    Log.debug(config, "OTP entered successfully on attempt " + (attempt + 1));
                    return;
                }
            }
            catch (Exception e)
            {
                Log.debug(config, "OTP attempt " + (attempt + 1) + " failed: " + e.getMessage());
            }
        }
        Log.warning(config, "OTP entry may have failed after " + maxRetries + " attempts");
    }

    public void inputOTP(String otp, Locator otpContainer)
    {
        inputOTP(otp, otpContainer, 3);
    }

    // ========== NAVIGATION ==========

    public void navigateTo(String url)
    {
        BrowserHelper.navigateTo(config, url);
    }

    public void navigateToFeature(Locator menuItem, String featureName)
    {
        Log.step(config, "Navigating to: " + featureName);
        click(menuItem, featureName + " menu item");
        waitForLoadingComplete();
    }

    // ========== SCROLL ==========

    public void scrollToElement(Locator locator, String elementName)
    {
        Element.scrollToElement(config, locator, elementName);
    }

    // ========== KEYBOARD ==========

    public void pressKey(String key)
    {
        page.keyboard().press(key);
    }
}
