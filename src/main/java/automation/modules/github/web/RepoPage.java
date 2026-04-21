package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

/**
 * GitHub repository page.
 * Verifies repository content including README and embedded images.
 */
public class RepoPage extends BasePage
{

    private final Locator readmeSection;
    private final Locator testCoverageImage;

    public RepoPage(Config config)
    {
        super(config);
        readmeSection     = page.locator("#readme");
        testCoverageImage = page.locator("img[src*='testrailPage1']");
        assertPageLoaded(readmeSection);
    }

    public boolean isReadmeVisible()
    {
        return isElementDisplayed(readmeSection);
    }

    public boolean isTestCoverageImageVisible()
    {
        scrollToElement(readmeSection, "README section");
        return isElementDisplayed(testCoverageImage);
    }
}
