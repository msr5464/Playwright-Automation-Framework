package automation.modules.github.web;

import automation.core.BasePage;
import automation.core.Config;
import com.microsoft.playwright.Locator;

/**
 * Page object for a GitHub repository page.
 * Covers the README section and embedded images within it.
 */
public class RepoPage extends BasePage
{
    private final Locator repoHeader = page.locator("#repository-container-header, main[id], main");
    private final Locator readmeSection = page.locator("#readme");
    private final Locator testCoverageImage = page.locator("img[src*='testrailPage1']");

    public RepoPage(Config config)
    {
        super(config);
        assertPageLoaded(repoHeader);
    }

    /**
     * Returns true if the README section is visible on the repository page.
     */
    public boolean isReadmeVisible()
    {
        return isElementDisplayed(readmeSection);
    }

    /**
     * Returns true if the Test Coverage Data image is visible in the README.
     */
    public boolean isTestCoverageImageVisible()
    {
        return isElementDisplayed(testCoverageImage);
    }
}
