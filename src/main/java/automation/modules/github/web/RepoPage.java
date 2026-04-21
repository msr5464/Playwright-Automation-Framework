package automation.modules.github.web;

import automation.core.BasePage;
import automation.core.Config;
import com.microsoft.playwright.Locator;

/**
 * Page object for a GitHub repository page.
 * Covers the README section and its embedded images.
 */
public class RepoPage extends BasePage
{
    private final Locator readmeSection;
    private final Locator testCoverageImage;

    public RepoPage(Config config)
    {
        super(config);
        readmeSection    = page.locator("#readme");
        testCoverageImage = page.locator("img[src*='testrailPage1']");
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
     * Returns true if the Test Coverage Data image is visible inside the README.
     */
    public boolean isTestCoverageImageVisible()
    {
        return isElementDisplayed(testCoverageImage);
    }
}
