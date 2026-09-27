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
        amountDisplay = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#txn_amount");
        otpField      = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#otp");
        submitButton  = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("button[name=\"ok\"]");
        assertPageLoaded(otpField);
    }

    public String getAmount()
    {
        return getText(amountDisplay, "Amount display");
    }

    public void enterOtp(String otp)
    {
        Log.comment(config, "Entering OTP");
        fillText(otpField, otp, "OTP field");
    }

    public PaymentSuccessPage submit()
    {
        Log.comment(config, "Submitting OTP");
        click(submitButton, "Submit button");
        return new PaymentSuccessPage(config);
    }
}
