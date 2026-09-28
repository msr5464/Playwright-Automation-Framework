package automation.modules.paymentgateway.web;

import com.microsoft.playwright.FrameLocator;
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
        FrameLocator threeDsFrame = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']");
        amountDisplay   = threeDsFrame.locator("#txn_amount");
        otpField        = threeDsFrame.locator("#otp");
        submitOtpButton = threeDsFrame.locator("button[name='ok']");
        assertPageLoaded(amountDisplay);
    }

    public String getAmount()
    {
        return getText(amountDisplay, "Transaction amount");
    }

    public IssuingBankPage enterOtp(String otp)
    {
        Log.comment(config, "Entering bank OTP");
        fillText(otpField, otp, "OTP field");
        return this;
    }

    public PaymentSuccessPage submitOtp()
    {
        Log.comment(config, "Submitting OTP to complete payment");
        click(submitOtpButton, "Submit OTP button");
        return new PaymentSuccessPage(config);
    }
}
