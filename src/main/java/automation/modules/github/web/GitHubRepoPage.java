package automation.modules.github.web;

import automation.core.BasePage;
import automation.core.Config;
import com.microsoft.playwright.Locator;

/**
 * GitHub repository page.
 * Provides access to repository details such as the sidebar description and the current URL.
 * Used to cross-verify data captured from the GitHub Repository API.
 */
public class GitHubRepoPage extends BasePage
{
    private final Locator repositoryDescription = page.locator("p[class*='SidebarAbout-module__description']");

    public GitHubRepoPage(Config config)
    {
        super(config);
        assertPageLoaded(repositoryDescription);
    }

    /**
     * Get the repository description shown in the sidebar About section.
     *
     * @return the repository description text
     */
    public String getRepositoryDescription()
    {
        return getText(repositoryDescription, "Repository description");
    }

    /**
     * Get the current URL of the browser page.
     *
     * @return the current page URL as a string
     */
    public String getCurrentUrl()
    {
        return page.url();
    }
}
