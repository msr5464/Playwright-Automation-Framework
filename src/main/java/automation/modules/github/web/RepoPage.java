package automation.modules.github.web;

import automation.core.BasePage;
import automation.core.Config;
import com.microsoft.playwright.Locator;

/**
 * Page object for a GitHub repository page.
 * Covers the repository main view including the README section.
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
     * Returns true if the FCT Test Coverage Data screenshot is visible in the README.
     */
    public boolean isFctCoverageScreenshotVisible()
    {
        return isElementDisplayed(fctCoverageScreenshot);
    }
}
