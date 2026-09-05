package automation.modules.naukari.web;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.WaitHelper;
import automation.core.BasePage;
import com.microsoft.playwright.Locator;

/**
 * Page object for the Naukri login page.
 * Handles credential entry and form submission.
 */
public class NaukriLoginPage extends BasePage
{
    private final Locator usernameField = page.locator("[id='usernameField']");
    private final Locator passwordField = page.locator("[id='passwordField']");
    private final Locator loginButton   = page.locator("button[type='submit'].blue-btn");

    public NaukriLoginPage(Config config)
    {
        super(config);
        assertPageLoaded(usernameField);
    }

    /**
     * Enter credentials, submit the login form, wait for the post-login redirect to
     * settle, then navigate to the profile page.
     *
     * @param username Naukri account email / username
     * @param password Naukri account password
     * @return NaukriProfilePage once the profile page is loaded
     */
    public NaukriProfilePage doLogin(String username, String password)
    {
        fillText(usernameField, username, "Username field");
        fillText(passwordField, password, "Password field");
        click(loginButton, "Login button");
        WaitHelper.waitForNetworkIdle(config);
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("naukari.profile.url"));
        return new NaukriProfilePage(config);
    }
}
