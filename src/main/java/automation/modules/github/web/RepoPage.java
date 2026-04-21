package automation.modules.github.web;

import automation.core.BasePage;
import automation.core.Config;
import com.microsoft.playwright.Locator;

/**
 * Page object for a GitHub repository page.
 * Covers the README section and embedded image assertions.
 */
public class RepoPage extends BasePage
{
    private final Locator readmeSection = page.locator("#readme");
    private final Locator testCoverageImage = page.locator("img[src*='testrailPage1']");

    public RepoPage(Config config)
    {
        super(config);
        assertPageLoaded(readmeSection);
    }

    /**
     * Returns true if the README section is visible on the repository page.
     */
    public boolean isReadmeVisible()
    {
        return isElementDisplayed(readmeSection);
    }

    /**
     * Scrolls to and checks whether the Test Coverage Data image is visible in the README.
     */
    public boolean isTestCoverageImageVisible()
    {
        scrollToElement(testCoverageImage, "Test Coverage Data image");
        return isElementDisplayed(testCoverageImage);
    }
}
