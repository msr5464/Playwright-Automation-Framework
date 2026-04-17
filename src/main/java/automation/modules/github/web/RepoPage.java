package automation.modules.github.web;

import automation.core.Config;
import automation.core.BasePage;
import com.microsoft.playwright.Locator;

/**
 * Page object for a GitHub repository landing page.
 * Covers the repository home view including the rendered README section.
 */
public class RepoPage extends BasePage
{
    private final Locator readmeArticle = page.locator("article.markdown-body");
    private final Locator fctCoverageScreenshot = page.locator("img[src*='testrailPage1']");

    public RepoPage(Config config)
    {
        super(config);
        assertPageLoaded(readmeArticle);
    }

    /**
     * Returns whether the FCT Test Coverage Data screenshot is visible in the README.
     */
    public boolean isFctCoverageScreenshotVisible()
    {
        return isElementDisplayed(fctCoverageScreenshot);
    }
}
