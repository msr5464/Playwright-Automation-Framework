package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

/**
 * Page object for a GitHub repository page.
 * Covers the repository README view, including the Test Coverage section.
 */
public class RepoPage extends BasePage
{
    private final Locator markdownBody      = page.locator("article.markdown-body");
    private final Locator testCoverageHeader = page.locator("div.markdown-heading:has(a#user-content-test-coverage-data-for-all-the-projects) h3");
    private final Locator testCoverageImage  = page.locator("img[src*='testrailPage1.png']");

    public RepoPage(Config config)
    {
        super(config);
        assertPageLoaded(markdownBody);
    }

    /**
     * Scroll the page to bring the Test Coverage section header into view.
     */
    public void scrollToTestCoverageSection()
    {
        config.logComment("Scrolling to Test Coverage section");
        scrollToElement(testCoverageHeader, "Test Coverage section header");
    }

    /**
     * Returns true if the test coverage image is currently visible on the page.
     */
    public boolean isTestCoverageImageVisible()
    {
        return isElementDisplayed(testCoverageImage);
    }
}
