package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

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

    /**
     * Returns the amount shown on the Issuing Bank page as plain number text
     * (no currency symbol, no grouping separators, no trailing decimal zeros).
     */
    public String getAmount()
    {
        String rawAmount = getText(amountDisplay, "Bank page amount");
        String digitsOnly = rawAmount.replaceAll("[^0-9.]", "");
        if (digitsOnly.contains("."))
        {
            digitsOnly = digitsOnly.replaceAll("\\.0+$", "");
            digitsOnly = digitsOnly.replaceAll("\\.$", "");
        }
        return digitsOnly;
    }

    public PaymentSuccessPage enterOtpAndSubmit(String otp)
    {
        fillText(otpField, otp, "OTP field");
        click(submitOtpButton, "Submit OTP button");
        return new PaymentSuccessPage(config);
    }
}
