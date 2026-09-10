package automation.modules.naukari.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;

public class NaukriLoginPage extends BasePage
{
    private final Locator emailField;
    private final Locator passwordField;
    private final Locator loginButton;

    public NaukriLoginPage(Config config)
    {
        super(config);
        emailField    = page.locator("[id='usernameField']");
        passwordField = page.locator("[id='passwordField']");
        loginButton   = page.locator("button[type='submit']:first-of-type");
        assertPageLoaded(emailField);
    }

    public void doLogin(String email, String password)
    {
        Log.comment(config, "Logging in to Naukri as: " + email);
        fillText(emailField, email, "Email field");
        fillText(passwordField, password, "Password field");
        click(loginButton, "Login button");
        WaitHelper.waitForNetworkIdle(config);
    }
}
