package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

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
        return normalizeAmount(getText(amountDisplay, "Amount display"));
    }

    public PaymentSuccessPage enterOtpAndSubmit(String otp)
    {
        fillText(otpField, otp, "OTP field");
        click(submitOtpButton, "Submit OTP button");
        WaitHelper.waitForNetworkIdle(config);
        return new PaymentSuccessPage(config);
    }

    /**
     * Reduces a displayed amount ('Rp50.000', '49000.00') to plain number text
     * ('50000', '49000') - no currency symbol, no thousands separator, no trailing
     * decimal zeros - so two differently-formatted amounts can be compared as strings.
     */
    private String normalizeAmount(String rawAmount)
    {
        String digitsAndDot = rawAmount.replaceAll("[^0-9.]", "");
        int lastDot = digitsAndDot.lastIndexOf('.');
        if (lastDot == -1)
        {
            return digitsAndDot;
        }
        String afterDot = digitsAndDot.substring(lastDot + 1);
        if (afterDot.length() == 2)
        {
            return digitsAndDot.substring(0, lastDot);
        }
        return digitsAndDot.replace(".", "");
    }
}
