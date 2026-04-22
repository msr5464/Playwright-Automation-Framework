package automation.modules.github.web;

import automation.core.BasePage;
import automation.core.Config;
import com.microsoft.playwright.Locator;

/**
 * Page object for a GitHub repository page.
 * Covers the README section and embedded images.
 */
public class RepoPage extends BasePage
{
    private final Locator repoHeader;
    private final Locator readmeSection;
    private final Locator testCoverageImage;

    public RepoPage(Config config)
    {
        super(config);
        repoHeader = page.locator("main");
        readmeSection = page.locator("article.markdown-body");
        testCoverageImage = page.locator("img[src*='testrailPage1']");
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
     * Returns true if the Test Coverage Data image is visible within the README.
     */
    public boolean isTestCoverageImageVisible()
    {
        return isElementDisplayed(testCoverageImage);
    }
}
