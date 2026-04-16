package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

/**
 * Page object for a GitHub repository page.
 * Covers the README section and embedded images within it.
 */
public class RepoPage extends BasePage
{

    private final Locator readmeSection;
    private final Locator fctCoverageScreenshot;

    public RepoPage(Config config)
    {
        super(config);
        readmeSection        = page.locator("article.markdown-body");
        fctCoverageScreenshot = page.locator("article.markdown-body img[alt='Testrail Page']");
        assertPageLoaded(readmeSection);
    }

    /**
     * Returns true if the README markdown body is visible on the page.
     */
    public boolean isReadmeVisible()
    {
        return isElementDisplayed(readmeSection);
    }

    /**
     * Returns true if the FCT Test Coverage screenshot image is visible within the README.
     * Scrolls directly to the image element first to ensure it is in the viewport,
     * then waits up to 5 seconds for it to become visible (handles lazy-loaded images).
     */
    public boolean isFctCoverageScreenshotVisible()
    {
        scrollToElement(fctCoverageScreenshot, "FCT Coverage Screenshot");
        return WaitHelper.waitForOptionalElementToBeVisible(config, fctCoverageScreenshot, "FCT Coverage Screenshot");
    }

    /**
     * Scrolls the viewport to bring the README section into view.
     */
    public void scrollToReadme()
    {
        scrollToElement(readmeSection, "README section");
    }
}
