package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

/**
 * Page object for a GitHub repository page.
 * Covers the repository landing view including the README section.
 */
public class RepoPage extends BasePage
{

    private final Locator readmeArticle;
    private final Locator fctCoverageScreenshot;

    public RepoPage(Config config)
    {
        super(config);
        readmeArticle       = page.locator("article.markdown-body");
        fctCoverageScreenshot = page.locator("img[src*='testrailPage1']");
        assertPageLoaded(readmeArticle);
    }

    /**
     * Returns true if the FCT Test Coverage Data screenshot is visible in the README.
     */
    public boolean isFctCoverageScreenshotVisible()
    {
        return isElementDisplayed(fctCoverageScreenshot);
    }
}
