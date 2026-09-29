package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class PaymentGatewayIssuingBankPage extends BasePage
{
    private final Locator amountDisplay;
    private final Locator otpField;
    private final Locator otpSubmitButton;

    public PaymentGatewayIssuingBankPage(Config config)
    {
        super(config);
        amountDisplay = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#txn_amount");
        otpField = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#otp");
        otpSubmitButton = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("button[name='ok']");
        assertPageLoaded(amountDisplay);
    }

    public String getDisplayedAmount()
    {
        return PaymentGatewayPaymentPopupPage.normalizeAmount(getText(amountDisplay, "Issuing bank amount display"));
    }

    public void enterOtp(String otp)
    {
        fillText(otpField, otp, "OTP field");
    }

    public PaymentGatewaySuccessPage submitOtp()
    {
        Log.comment(config, "Submitting OTP on the issuing bank page");
        click(otpSubmitButton, "OTP submit button");
        return new PaymentGatewaySuccessPage(config);
    }
}
