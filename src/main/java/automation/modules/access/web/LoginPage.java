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
        emailField = page.locator("input[type='email'], input[name='email'], input[placeholder*='email'], input[placeholder*='Email']");
        passwordField = page.locator("input[type='password'], input[name='password'], input[placeholder*='password'], input[placeholder*='Password']");
        loginButton = page.locator("button[type='submit'], button:has-text('Login'), button:has-text('Sign in'), input[type='submit']");
        otpContainer = page.locator("[data-cy*='otp'], .otp-container, input[placeholder*='OTP'], input[placeholder*='code']");
        dashboardIndicator = page.locator("[data-cy*='dashboard'], .dashboard-container, [class*='dashboard']");
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
