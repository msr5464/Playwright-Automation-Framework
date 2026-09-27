package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class CreditCardFormPage extends BasePage
{
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator promoDropdown;
    private final Locator amountDisplay;
    private final Locator continueButton;

    public CreditCardFormPage(Config config)
    {
        super(config);
        cardNumberField = page.frameLocator("#snap-midtrans").locator("input[placeholder='1234 1234 1234 1234']");
        expiryField     = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField        = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        promoDropdown   = page.frameLocator("#snap-midtrans").locator(".promo-selection");
        amountDisplay   = page.frameLocator("#snap-midtrans").locator(".header-amount");
        continueButton  = page.frameLocator("#snap-midtrans").locator("button.btn-theme");
        assertPageLoaded(cardNumberField);
    }

    public void enterCardNumber(String cardNumber)
    {
        Log.comment(config, "Entering card number");
        fillText(cardNumberField, cardNumber, "Card number field");
    }

    public void enterExpiry(String expiry)
    {
        Log.comment(config, "Entering expiry: " + expiry);
        fillText(expiryField, expiry, "Expiry field");
    }

    public void enterCvv(String cvv)
    {
        Log.comment(config, "Entering CVV");
        fillText(cvvField, cvv, "CVV field");
    }

    public void selectPromo(String promoText)
    {
        Log.comment(config, "Selecting promo: " + promoText);
        selectOption(promoDropdown, promoText, "Promo dropdown");
    }

    public String getAmountAfterPromo()
    {
        return getText(amountDisplay, "Amount display after promo");
    }

    public IssuingBankPage clickContinue()
    {
        Log.comment(config, "Clicking Pay now to proceed to issuing bank page");
        click(continueButton, "Pay now button");
        return new IssuingBankPage(config);
    }
}
