package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.modules.paymentgateway.PaymentGatewayHelper;

public class IssuingBankPage extends BasePage {
    private final Locator amountDisplay;
    private final Locator otpField;
    private final Locator otpSubmitButton;

    public IssuingBankPage(Config config) {
        super(config);
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        otpField = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#mukesh");
        otpSubmitButton = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']")
                .locator("button[name='ok']");
        assertPageLoaded(amountDisplay);
    }

    public String getAmount() {
        return PaymentGatewayHelper.toPlainAmount(getText(amountDisplay, "Issuing bank amount"));
    }

    public PaymentSuccessPage enterOtpAndFinish(String otp) {
        fillText(otpField, otp, "OTP field");
        click(otpSubmitButton, "Submit button");
        return new PaymentSuccessPage(config);
    }
}
