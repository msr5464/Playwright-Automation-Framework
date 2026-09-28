package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class IssuingBankPage extends BasePage
{
    private final Locator bankAmountText;
    private final Locator otpField;
    private final Locator submitOtpButton;

    public IssuingBankPage(Config config)
    {
        super(config);
        bankAmountText = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#txn_amount");
        otpField = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#otp");
        submitOtpButton = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("button[name='ok']");
        assertPageLoaded(bankAmountText);
    }

    public String getBankAmountText()
    {
        return getText(bankAmountText, "Bank transaction amount");
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
