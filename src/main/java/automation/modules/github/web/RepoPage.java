package automation.modules.github.web;

import automation.core.Config;
import automation.core.BasePage;
import com.microsoft.playwright.Locator;

/**
 * Page object for a GitHub repository page.
 * Covers the README section and screenshot verification within the rendered README.
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
     * Scrolls to the FCT Test Coverage Data screenshot in the README and checks its visibility.
     *
     * @return true if the screenshot image is present and visible, false otherwise
     */
    public boolean isFctCoverageScreenshotVisible()
    {
        scrollToElement(fctCoverageScreenshot, "FCT Coverage Screenshot");
        return isElementDisplayed(fctCoverageScreenshot);
    }
}
