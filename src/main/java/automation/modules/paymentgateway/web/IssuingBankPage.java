package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class IssuingBankPage extends BasePage
{
    private final Locator amountDisplay;
    private final Locator otpField;
    private final Locator submitOtpButton;

    public IssuingBankPage(Config config)
    {
        super(config);
        amountDisplay   = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#txn_amount");
        otpField        = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("input[name='otp']");
        submitOtpButton = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("button[name='ok']");
        assertPageLoaded(otpField);
    }

    public String getAmount()
    {
        return getText(amountDisplay, "Amount display");
    }

    public void enterOtp(String otp)
    {
        Log.comment(config, "Entering bank OTP");
        fillText(otpField, otp, "OTP field");
    }

    public PaymentSuccessPage submitOtp()
    {
        Log.comment(config, "Submitting OTP to complete payment");
        click(submitOtpButton, "Submit OTP button");
        return new PaymentSuccessPage(config);
    }
}
