package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

/**
 * Page object for a GitHub repository page.
 * Provides access to repository content sections such as the Test Coverage block.
 */
public class RepoPage extends BasePage
{

    private final Locator testCoverageHeader;
    private final Locator testCoverageImage;

    public RepoPage(Config config)
    {
        super(config);
        testCoverageHeader = page.locator("div.markdown-heading:has(a#user-content-test-coverage-data-for-all-the-projects) h3");
        testCoverageImage  = page.locator("img[src*='testrailPage1.png']");
        assertPageLoaded(testCoverageHeader);
    }

    /**
     * Scroll the browser viewport to the Test Coverage section header.
     */
    public void scrollToTestCoverageSection()
    {
        scrollToElement(testCoverageHeader, "Test Coverage Header");
    }

    /**
     * Check whether the Test Coverage image is visible on the page.
     *
     * @return true if the image is displayed, false otherwise
     */
    public boolean isTestCoverageImageVisible()
    {
        return isElementDisplayed(testCoverageImage);
    }
}
