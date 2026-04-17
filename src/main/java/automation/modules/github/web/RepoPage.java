package automation.modules.github.web;

import automation.core.BasePage;
import automation.core.Config;
import com.microsoft.playwright.Locator;

/**
 * Page object for a GitHub repository page.
 * Covers README inspection and screenshot visibility assertions.
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
     * Returns true if the README section is currently visible on the page.
     */
    public boolean isReadmeVisible()
    {
        return isElementDisplayed(readmeSection);
    }

    /**
     * Returns true if the FCT Test Coverage screenshot (alt='Testrail Page') is visible inside the README.
     */
    public boolean isFctCoverageScreenshotVisible()
    {
        return isElementDisplayed(fctCoverageScreenshot);
    }

    /**
     * Scrolls the page until the README section is in view.
     *
     * @return this RepoPage for fluent chaining
     */
    public RepoPage scrollToReadme()
    {
        scrollToElement(readmeSection, "README section");
        return this;
    }
}
