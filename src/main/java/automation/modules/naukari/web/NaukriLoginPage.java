package automation.modules.naukari.web;

import automation.core.BasePage;
import automation.core.Config;
import com.microsoft.playwright.Locator;

/**
 * Naukri login page — fills credentials, submits the form, then navigates directly
 * to the profile page so callers receive a ready-to-use NaukriProfilePage.
 */
public class NaukriLoginPage extends BasePage
{
    private static final String PROFILE_URL = "https://www.naukri.com/mnjuser/profile";

    private final Locator usernameField = page.locator("[id='usernameField']");
    private final Locator passwordField = page.locator("[id='passwordField']");
    private final Locator loginButton   = page.locator("button.blue-btn[type='submit']");

    public NaukriLoginPage(Config config)
    {
        super(config);
        assertPageLoaded(usernameField);
    }

    /**
     * Enter credentials, submit the login form, navigate to the profile page,
     * and return a NaukriProfilePage ready for interaction.
     *
     * @param username Naukri account email / username
     * @param password Naukri account password
     * @return NaukriProfilePage once the profile page has loaded
     */
    public NaukriProfilePage doLogin(String username, String password)
    {
        fillText(usernameField, username, "Username field");
        fillText(passwordField, password, "Password field");
        click(loginButton, "Login button");
        page.navigate(PROFILE_URL);
        return new NaukriProfilePage(config);
    }
}
