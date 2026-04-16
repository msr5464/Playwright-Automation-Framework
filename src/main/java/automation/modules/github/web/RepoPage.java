package automation.modules.github.web;

import automation.core.Config;
import automation.core.web.BasePage;
import com.microsoft.playwright.Locator;

/**
 * Page object for a GitHub repository landing page.
 * Covers the README section and embedded screenshots.
 */
public class RepoPage extends BasePage
{
    private final Locator readmeSection = page.locator("article.markdown-body");
    private final Locator fctCoverageScreenshot = page.locator("article.markdown-body img[alt='Testrail Page']");

    public RepoPage(Config config)
    {
        super(config);
        assertPageLoaded(readmeSection);
    }

    /**
     * Returns true if the README article body is visible on the page.
     */
    public boolean isReadmeVisible()
    {
        return isElementDisplayed(readmeSection);
    }

    /**
     * Returns true if the FCT Coverage screenshot (alt='Testrail Page') is visible in the README.
     */
    public boolean isFctCoverageScreenshotVisible()
    {
        return isElementDisplayed(fctCoverageScreenshot);
    }

    /**
     * Scrolls the viewport to the README article section.
     *
     * @return this RepoPage for fluent chaining
     */
    public RepoPage scrollToReadme()
    {
        scrollToElement(readmeSection, "README section");
        return this;
    }
}
