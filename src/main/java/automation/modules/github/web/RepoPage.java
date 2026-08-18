package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

/**
 * Page object for a GitHub repository page.
 * Covers the repository sidebar and description area.
 */
public class RepoPage extends BasePage
{
    private final Locator repoDescription;

    public RepoPage(Config config)
    {
        super(config);
        repoDescription = page.locator("[class*='SidebarAbout-module__description']");
        assertPageLoaded(repoDescription);
    }

    /**
     * Return the repository description text shown in the sidebar about section.
     */
    public String getDescription()
    {
        return getText(repoDescription, "Repository description");
    }
}
