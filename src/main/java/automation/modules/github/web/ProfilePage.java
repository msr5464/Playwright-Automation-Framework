package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

/**
 * Page object for a GitHub public user profile page (github.com/{username}).
 * Provides access to the bio text and profile avatar image.
 */
public class ProfilePage extends BasePage
{
    private final Locator bioText;
    private final Locator avatarImage;

    /**
     * Constructs a ProfilePage for the given GitHub username.
     * Locators are built using the username so the avatar alt-text selector
     * matches the exact element confirmed by DOM inspection.
     *
     * @param config   test config
     * @param username GitHub login/username whose profile is open in the browser
     */
    public ProfilePage(Config config, String username)
    {
        super(config);
        this.bioText     = page.locator(".p-note");
        this.avatarImage = page.locator("img[alt=\"View " + username + "'s full-sized avatar\"]");
        assertPageLoaded(avatarImage);
    }

    /**
     * Return the bio text displayed on the profile page.
     * Returns an empty string when the user has no bio set.
     */
    public String getBioText()
    {
        if (!isElementDisplayed(bioText)) {
            return "";
        }
        return getText(bioText, "Bio text");
    }

    /**
     * Check whether the profile avatar image is visible on the page.
     */
    public boolean isAvatarVisible()
    {
        return isElementDisplayed(avatarImage);
    }
}
