package automation.modules.orangehrm.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class LoginPage extends BasePage
{
    private final Locator usernameField;
    private final Locator passwordField;
    private final Locator loginButton;

    public LoginPage(Config config)
    {
        super(config);
        usernameField = page.locator("input[name='username']");
        passwordField = page.locator("input[name='password']");
        loginButton   = page.locator("button[type='submit']");
        assertPageLoaded(loginButton);
    }

    public Locator getLoginButton()
    {
        return loginButton;
    }

    public DashboardPage login(String username, String password)
    {
        Log.comment(config, "Logging in to OrangeHRM as: " + username);
        fillText(usernameField, username, "Username field");
        fillText(passwordField, password, "Password field");
        click(loginButton, "Login button");
        return new DashboardPage(config);
    }
}
