package automation.modules.naukari.web;

import automation.core.BasePage;
import automation.core.Config;
import com.microsoft.playwright.Locator;

/**
 * Naukri login page — fills credentials and submits the login form.
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
     * Fills credentials and clicks the Login button.
     * Does not wait for the post-login redirect — the caller is responsible for waiting
     * before issuing any subsequent navigation.
     */
    public void doLogin(String username, String password)
    {
        fillText(usernameField, username, "Username field");
        fillText(passwordField, password, "Password field");
        click(loginButton, "Login button");
    }
}
