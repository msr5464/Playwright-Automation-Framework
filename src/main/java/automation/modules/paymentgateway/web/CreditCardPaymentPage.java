package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class CreditCardPaymentPage extends BasePage
{
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator promoDropdown;
    private final Locator continueButton;
    private final Locator amountDisplay;

    public CreditCardPaymentPage(Config config)
    {
        super(config);
        cardNumberField = page.frameLocator("#snap-midtrans").locator("div.card-number-input-container input[type='tel']");
        expiryField = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        promoDropdown = page.frameLocator("#snap-midtrans").locator("label[for='690']");
        continueButton = page.frameLocator("#snap-midtrans").locator("button.btn.full.primary.btn-theme");
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        assertPageLoaded(cardNumberField);
    }

    public void fillCardNumber(String cardNumber)
    {
        fillText(cardNumberField, cardNumber, "Card number field");
    }

    public void fillExpiry(String expiry)
    {
        fillText(expiryField, expiry, "Card expiry field");
    }

    public void fillCvv(String cvv)
    {
        fillText(cvvField, cvv, "Card CVV field");
    }

    public void selectPromo(String promoName)
    {
        Log.comment(config, "Selecting promo: " + promoName);
        click(promoDropdown, "Promo option: " + promoName);
    }

    public String getAmountDisplayed()
    {
        return getText(amountDisplay, "Amount display");
    }

    public IssuingBankPage clickContinue()
    {
        Log.comment(config, "Clicking Continue to proceed to the issuing bank page");
        click(continueButton, "Continue button");
        return new IssuingBankPage(config);
    }
}
