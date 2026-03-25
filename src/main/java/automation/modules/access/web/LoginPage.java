package automation.modules.access.web;

import com.microsoft.playwright.Locator;

import automation.core.User;
import automation.core.Config;
import automation.core.BasePage;
import automation.core.Log;
import automation.core.WaitHelper;

public class LoginPage extends BasePage
{

    private final Locator emailField;
    private final Locator passwordField;
    private final Locator loginButton;
    private final Locator otpContainer;
    private final Locator dashboardIndicator;

    public LoginPage(Config config)
    {
        super(config);
        emailField = page.locator("[data-cy='login-email'], input[name='email']");
        passwordField = page.locator("[data-cy='login-password'], input[name='password']");
        loginButton = page.locator("[data-cy='login-button'], button[type='submit']");
        otpContainer = page.locator("[data-cy='otp-input'], .otp-container");
        dashboardIndicator = page.locator("[data-cy='dashboard'], .dashboard-container");
    }

    public void enterEmail(String email)
    {
        fillText(emailField, email, "Email field");
    }

    public void enterPassword(String password)
    {
        fillText(passwordField, password, "Password field");
    }

    public void clickLogin()
    {
        click(loginButton, "Login button");
    }

    public void enterOtp(String otp)
    {
        WaitHelper.waitForElementToBeVisible(config, otpContainer, "OTP container");
        inputOTP(otp, otpContainer);
    }

    public void login(User user)
    {
        Log.step(config, "Logging in as: " + user.getUsername());
        enterEmail(user.getUsername());
        enterPassword(user.getPassword());
        clickLogin();
        waitForLoadingComplete();

        if (user.getOtp() != null)
        {
            enterOtp(user.getOtp());
            waitForLoadingComplete();
        }

        WaitHelper.waitForElementToBeVisible(config, dashboardIndicator, "Dashboard");
        Log.pass(config, "Login successful");
    }
}
