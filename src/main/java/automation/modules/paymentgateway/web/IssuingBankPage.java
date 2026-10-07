package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.modules.paymentgateway.PaymentGatewayHelper;

public class IssuingBankPage extends BasePage
{
    private final Locator amountDisplay;
    private final Locator otpField;
    private final Locator submitOtpButton;

    public IssuingBankPage(Config config)
    {
        super(config);
        amountDisplay = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#txn_amount");
        otpField = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#otp");
        submitOtpButton = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("button[name='ok']");
        assertPageLoaded(amountDisplay);
    }

    public String getAmount()
    {
        return PaymentGatewayHelper.toPlainAmount(getText(amountDisplay, "Issuing bank amount display"));
    }

    public PaymentSuccessPage enterOtp(String otp)
    {
        Log.comment(config, "Entering bank OTP to finish the payment");
        fillText(otpField, otp, "OTP field");
        click(submitOtpButton, "Submit OTP button");
        return new PaymentSuccessPage(config);
    }
}
