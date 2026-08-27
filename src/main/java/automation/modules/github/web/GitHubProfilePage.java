package automation.modules.github.web;

import automation.core.BasePage;
import automation.core.Config;
import com.microsoft.playwright.Locator;

/**
 * GitHub public profile page.
 * Provides access to profile details such as location text and avatar image.
 * Used to cross-verify data captured from the GitHub User API.
 */
public class GitHubProfilePage extends BasePage
{
    private final Locator locationText = page.locator("[aria-label^='Home location:']");
    private final Locator avatarImage  = page.locator("img[alt$='full-sized avatar']");

    public GitHubProfilePage(Config config)
    {
        super(config);
        assertPageLoaded(avatarImage);
    }

    /**
     * Get the location text displayed on the profile page.
     *
     * @return the location string shown on the profile (e.g. "San Francisco, CA")
     */
    public String getLocationText()
    {
        return getText(locationText, "Location text");
    }

    /**
     * Check whether the profile avatar image is visible on the page.
     *
     * @return true if the avatar image element is displayed
     */
    public boolean isAvatarVisible()
    {
        return isElementDisplayed(avatarImage);
    }

    /**
     * Get the src URL of the profile avatar image.
     *
     * @return the value of the avatar img src attribute
     */
    public String getAvatarImageUrl()
    {
        return avatarImage.getAttribute("src");
    }
}
