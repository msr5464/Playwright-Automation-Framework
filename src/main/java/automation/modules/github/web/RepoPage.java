package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

/**
 * Page object for a GitHub repository page.
 * Covers the README section and content images rendered within it.
 */
public class RepoPage extends BasePage
{
    private final Locator readmeSection;
    private final Locator testCoverageImage;

    public RepoPage(Config config)
    {
        super(config);
        readmeSection     = page.locator("[id='readme']");
        testCoverageImage = page.locator("img[src*='testrailPage1']");
        assertPageLoaded(readmeSection);
    }

    /**
     * Returns true if the README section is visible on the page.
     */
    public boolean isReadmeVisible()
    {
        return isElementDisplayed(readmeSection);
    }

    /**
     * Scrolls to the Test Coverage Data image in the README and returns
     * whether it is visible.
     */
    public boolean isTestCoverageImageVisible()
    {
        scrollToElement(testCoverageImage, "Test Coverage Data image");
        return isElementDisplayed(testCoverageImage);
    }
}
