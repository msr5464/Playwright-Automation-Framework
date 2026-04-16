package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class OtpPage extends BasePage
{

    private final Locator otpInputField;
    private final Locator verifyButton;

    public OtpPage(Config config)
    {
        super(config);
        otpInputField = page.locator("input[name='otp']");
        verifyButton  = page.locator("button[type='submit']");
        assertPageLoaded(otpInputField);
    }

    public DashboardPage enterOtpAndVerify(String otp)
    {
        fillText(otpInputField, otp, "OTP input field");
        click(verifyButton, "Verify button");
        return new DashboardPage(config);
    }
}
