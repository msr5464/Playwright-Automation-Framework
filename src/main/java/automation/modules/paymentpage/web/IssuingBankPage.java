package automation.modules.paymentpage.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class IssuingBankPage extends BasePage
{
    private final Locator amountDisplay;
    private final Locator otpField;
    private final Locator submitButton;

    public IssuingBankPage(Config config)
    {
        super(config);
        amountDisplay = page.locator(".amount");
        otpField      = page.locator("#otp");
        submitButton  = page.locator("button[name='ok']");
        assertPageLoaded(otpField);
    }

    public String getAmountDisplay()
    {
        return getText(amountDisplay, "Amount display");
    }

    public IssuingBankPage enterOtp(String otp)
    {
        Log.comment(config, "Entering OTP");
        fillText(otpField, otp, "OTP field");
        return this;
    }

    public PaymentSuccessPage clickSubmit()
    {
        Log.comment(config, "Clicking Submit button");
        click(submitButton, "Submit button");
        return new PaymentSuccessPage(config);
    }
}
