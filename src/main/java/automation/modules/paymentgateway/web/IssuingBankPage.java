package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class IssuingBankPage extends BasePage
{
    private final Locator bankAmountDisplay;
    private final Locator otpField;
    private final Locator submitOtpButton;

    public IssuingBankPage(Config config)
    {
        super(config);
        bankAmountDisplay = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#txn_amount");
        otpField = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#otp");
        submitOtpButton = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("button[name='ok']");
        assertPageLoaded(bankAmountDisplay);
    }

    public String getBankAmount()
    {
        return getText(bankAmountDisplay, "Bank amount display");
    }

    public PaymentSuccessPage enterOtp(String otp)
    {
        fillText(otpField, otp, "OTP field");
        click(submitOtpButton, "Submit OTP button");
        return new PaymentSuccessPage(config);
    }
}
