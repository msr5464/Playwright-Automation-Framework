package automation.modules.saucedemo.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class LoginPage extends BasePage
{
    private final Locator usernameField;
    private final Locator passwordField;
    private final Locator loginButton;
    private final Locator errorMessage;

    public LoginPage(Config config)
    {
        super(config);
        usernameField = page.locator("#user-name");
        passwordField = page.locator("#password");
        loginButton   = page.locator("#login-button");
        errorMessage  = page.locator("[data-test='error']");
        assertPageLoaded(usernameField);
    }

    public ProductsPage doLogin(String username, String password)
    {
        Log.comment(config, "Logging in to SauceDemo as: " + username);
        fillText(usernameField, username, "Username field");
        fillText(passwordField, password, "Password field");
        click(loginButton, "Login button");
        return new ProductsPage(config);
    }

    public boolean isErrorDisplayed()
    {
        return isElementDisplayed(errorMessage);
    }

    public String getErrorMessage()
    {
        return getText(errorMessage, "Error message");
    }
}
