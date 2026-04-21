package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

/**
 * GitHub repository page.
 * Verifies repository content including README and embedded images.
 */
public class RepoPage extends BasePage
{

    private final Locator readmeSection;
    private final Locator testCoverageImage;
    private final Locator repoContainer;

    public RepoPage(Config config)
    {
        super(config);
        readmeSection     = page.locator("#readme");
        testCoverageImage = page.locator("img[src*='testrailPage1']");
        repoContainer     = page.locator(".repository-content");
        assertPageLoaded(repoContainer, readmeSection);
    }

    public boolean isReadmeVisible()
    {
        return isElementDisplayed(readmeSection);
    }

    public boolean isTestCoverageImageVisible()
    {
        return WaitHelper.waitForOptionalElementToBeVisible(config, testCoverageImage, "Test Coverage image");
    }
}
