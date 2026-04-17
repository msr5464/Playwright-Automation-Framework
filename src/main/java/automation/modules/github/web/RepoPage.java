package automation.modules.github.web;

import automation.core.Config;
import automation.core.web.BasePage;
import com.microsoft.playwright.Locator;

/**
 * Page object for a GitHub repository page.
 * Covers the repository landing page including the rendered README section.
 */
public class RepoPage extends BasePage
{
    private final Locator readmeSection;
    private final Locator fctCoverageScreenshot;

    public RepoPage(Config config)
    {
        super(config);
        readmeSection = page.locator("article.markdown-body");
        fctCoverageScreenshot = page.locator("article.markdown-body img[alt=\"Testrail Page\"]");
        assertPageLoaded(readmeSection);
    }

    /**
     * Returns true when the README markdown body is displayed on the page.
     */
    public boolean isReadmeVisible()
    {
        return isElementDisplayed(readmeSection);
    }

    /**
     * Returns true when the FCT Test Coverage screenshot (alt=\"Testrail Page\") is visible
     * inside the README section.
     */
    public boolean isFctCoverageScreenshotVisible()
    {
        return isElementDisplayed(fctCoverageScreenshot);
    }

    /**
     * Scrolls the viewport to the README section.
     */
    public void scrollToReadme()
    {
        scrollToElement(readmeSection, "README section");
    }
}
