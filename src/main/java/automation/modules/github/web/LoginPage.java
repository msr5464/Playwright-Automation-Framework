package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;

public class LoginPage extends BasePage
{

    private final Locator usernameField;
    private final Locator passwordField;
    private final Locator signInButton;

    public LoginPage(Config config)
    {
        super(config);
        usernameField = page.locator("#login_field");
        passwordField = page.locator("#password");
        signInButton  = page.locator("input[type='submit'][value='Sign in']");
        waitUntilLoaded();
    }

    @Override
    protected void waitUntilLoaded()
    {
        WaitHelper.waitForElementToBeVisible(config, usernameField, "Username field");
    }

    public void enterUsername(String username)
    {
        fillText(usernameField, username, "Username field");
    }

    public void enterPassword(String password)
    {
        fillText(passwordField, password, "Password field");
    }

    public void clickSignIn()
    {
        click(signInButton, "Sign In button");
    }

    /**
     * Enters credentials and clicks sign in.
     * Returns DashboardPage if login succeeds directly, or OtpPage if 2FA is required.
     */
    public DashboardPage doLogin(String username, String password)
    {
        Log.step(config, "Logging in to GitHub as: " + username);
        enterUsername(username);
        enterPassword(password);
        clickSignIn();
        return new DashboardPage(config);
    }

    public OtpPage doLoginExpectingOtp(String username, String password)
    {
        Log.step(config, "Logging in to GitHub (OTP expected) as: " + username);
        enterUsername(username);
        enterPassword(password);
        clickSignIn();
        return new OtpPage(config);
    }
}
