package automation.modules.paymentgateway.web;

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
        otpField = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#otp");
        submitButton = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("button[name='ok']");
        assertPageLoaded(amountDisplay);
    }

    public String getAmountDisplayed()
    {
        return getText(amountDisplay, "Amount display");
    }

    public void fillOtp(String otp)
    {
        fillText(otpField, otp, "OTP field");
    }

    public PaymentSuccessPage submit()
    {
        Log.comment(config, "Submitting OTP on the issuing bank page");
        click(submitButton, "Submit button");
        return new PaymentSuccessPage(config);
    }
}
