package automation.modules.naukari.web;

import automation.core.BasePage;
import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.WaitHelper;
import com.microsoft.playwright.Locator;

/**
 * Naukri login page — fills credentials, submits the form, then navigates
 * directly
 * to the profile page so callers receive a ready-to-use NaukriProfilePage.
 */
public class NaukriLoginPage extends BasePage {
    private final Locator usernameField = page.locator("[id='usernameField']");
    private final Locator passwordField = page.locator("[id='passwordField']");
    private final Locator loginButton = page.locator("button[type='submit']");

    public NaukriLoginPage(Config config) {
        super(config);
        assertPageLoaded(usernameField);
    }

    /**
     * Enter credentials, submit the login form, wait for the post-login redirect to
     * settle, navigate to the profile page, and return a NaukriProfilePage ready
     * for interaction.
     *
     * @param username Naukri account email / username
     * @param password Naukri account password
     * @return NaukriProfilePage once the profile page has loaded
     */
    public NaukriProfilePage doLogin(String username, String password) {
        fillText(usernameField, username, "Username field");
        fillText(passwordField, password, "Password field");
        click(loginButton, "Login button");
        WaitHelper.waitForUrl(config, "**/mnjuser/**");
        WaitHelper.waitForNetworkIdle(config);
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("naukari.profile.url"));
        return new NaukriProfilePage(config);
    }
}
