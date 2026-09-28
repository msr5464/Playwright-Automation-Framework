package automation.modules.paymentgateway.web;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class CreditCardFormPage extends BasePage
{
    private final FrameLocator snapFrame;
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator promoDropdown;
    private final Locator amountDisplay;
    private final Locator continueButton;

    public CreditCardFormPage(Config config)
    {
        super(config);
        snapFrame       = page.frameLocator("#snap-midtrans");
        cardNumberField = snapFrame.locator("input[placeholder='1234 1234 1234 1234']");
        expiryField     = snapFrame.locator("#card-expiry");
        cvvField        = snapFrame.locator("#card-cvv");
        promoDropdown   = snapFrame.locator(".promo-selection");
        amountDisplay   = snapFrame.locator(".header-amount");
        continueButton  = snapFrame.locator("button.btn-theme");
        assertPageLoaded(cardNumberField);
    }

    public CreditCardFormPage enterCardNumber(String cardNumber)
    {
        Log.comment(config, "Entering card number");
        fillText(cardNumberField, cardNumber, "Card number field");
        return this;
    }

    public CreditCardFormPage enterExpiry(String expiry)
    {
        Log.comment(config, "Entering card expiry: " + expiry);
        fillText(expiryField, expiry, "Expiry field");
        return this;
    }

    public CreditCardFormPage enterCvv(String cvv)
    {
        Log.comment(config, "Entering CVV");
        fillText(cvvField, cvv, "CVV field");
        return this;
    }

    public CreditCardFormPage selectPromo(String promoText)
    {
        Log.comment(config, "Selecting promo: " + promoText);
        click(promoDropdown, "Promo dropdown");
        Locator option = snapFrame.locator(".promo-option:has-text('" + promoText + "')");
        click(option, "Promo option: " + promoText);
        return this;
    }

    public String getAmountAfterPromo()
    {
        return getText(amountDisplay, "Amount display after promo");
    }

    public IssuingBankPage clickContinue()
    {
        Log.comment(config, "Clicking Pay Now / Continue button");
        click(continueButton, "Continue button");
        return new IssuingBankPage(config);
    }
}
