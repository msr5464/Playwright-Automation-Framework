package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class IssuingBankPage extends BasePage
{
    private final Locator amountText;
    private final Locator otpField;
    private final Locator confirmButton;

    public IssuingBankPage(Config config)
    {
        super(config);
        amountText = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#txn_amount");
        otpField = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("#otp");
        confirmButton = page.frameLocator("#snap-midtrans").frameLocator("iframe[title='3ds-iframe']").locator("button[name='ok']");
        assertPageLoaded(amountText);
    }

    /**
     * Returns the amount shown on the Issuing Bank page as a plain number string
     * (no currency symbol, no grouping separators, no trailing decimal zeros)
     * so it can be compared for equality with amounts from other pages.
     */
    public String getAmountText()
    {
        String raw = getText(amountText, "Issuing Bank amount");
        return normalizeAmount(raw);
    }

    public void fillOtp(String otp)
    {
        Log.comment(config, "Filling Bank OTP field");
        fillText(otpField, otp, "Bank OTP field");
    }

    public PaymentSuccessPage clickConfirm()
    {
        Log.comment(config, "Clicking Confirm button to finish the payment");
        click(confirmButton, "Confirm button");
        return new PaymentSuccessPage(config);
    }

    private static String normalizeAmount(String raw)
    {
        String cleaned = raw.replace("Rp", "").trim();
        cleaned = cleaned.replace(",", "");
        if (cleaned.matches(".*\\.\\d{2}$"))
        {
            cleaned = cleaned.substring(0, cleaned.lastIndexOf('.'));
        }
        else
        {
            cleaned = cleaned.replace(".", "");
        }
        return cleaned;
    }
}
