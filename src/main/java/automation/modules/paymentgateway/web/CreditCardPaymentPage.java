package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;

public class CreditCardPaymentPage extends BasePage
{
    private final Locator creditCardOption;
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator promoField;
    private final Locator continueButton;
    private final Locator amountText;

    public CreditCardPaymentPage(Config config)
    {
        super(config);
        creditCardOption = page.frameLocator("#snap-midtrans").locator("a[href='#/credit-card']");
        cardNumberField = page.frameLocator("#snap-midtrans").locator("div.card-number-input-container input[type='tel']");
        expiryField = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        promoField = page.frameLocator("#snap-midtrans").locator("label[for='690']");
        continueButton = page.frameLocator("#snap-midtrans").locator("button.btn.full.primary.btn-theme");
        amountText = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        assertPageLoaded(creditCardOption);
    }

    public void selectCreditCardMethod()
    {
        Log.comment(config, "Selecting Credit Card payment method");
        click(creditCardOption, "Credit Card option");
        WaitHelper.waitForElementToBeVisible(config, cardNumberField, "Card number field");
    }

    public void fillCardNumber(String cardNumber)
    {
        Log.comment(config, "Filling card number field");
        fillText(cardNumberField, cardNumber, "Card number field");
    }

    public void fillExpiry(String expiry)
    {
        Log.comment(config, "Filling card expiry field with: " + expiry);
        fillText(expiryField, expiry, "Card expiry field");
    }

    public void fillCvv(String cvv)
    {
        Log.comment(config, "Filling card CVV field");
        fillText(cvvField, cvv, "Card CVV field");
    }

    public void selectPromo()
    {
        Log.comment(config, "Selecting promo code");
        click(promoField, "Promo code option");
        WaitHelper.waitForNetworkIdle(config);
    }

    /**
     * Returns the amount shown on the credit card payment page as a plain number
     * string (no currency symbol, no grouping separators, no trailing decimal
     * zeros) so it can be compared for equality with amounts from other pages.
     */
    public String getAmountText()
    {
        String raw = getText(amountText, "Credit card payment amount");
        return normalizeAmount(raw);
    }

    public IssuingBankPage clickContinue()
    {
        Log.comment(config, "Clicking Continue button to proceed to the Issuing Bank page");
        click(continueButton, "Continue button");
        return new IssuingBankPage(config);
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
