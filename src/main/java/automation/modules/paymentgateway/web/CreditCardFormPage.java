package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayEnums.Promo;

public class CreditCardFormPage extends BasePage
{
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator amountDisplay;
    private final Locator continueButton;

    public CreditCardFormPage(Config config)
    {
        super(config);
        cardNumberField = page.frameLocator("#snap-midtrans").locator("div.card-number-input-container input[type='tel']");
        expiryField = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        continueButton = page.frameLocator("#snap-midtrans").locator("button.btn.full.primary.btn-theme");
        assertPageLoaded(amountDisplay);
    }

    public CreditCardFormPage fillCardDetails(PaymentGatewayData payment)
    {
        fillText(cardNumberField, payment.getCardNumber(), "Card number field");
        fillText(expiryField, payment.getExpiry(), "Expiry field");
        fillText(cvvField, payment.getCvv(), "CVV field");
        return this;
    }

    /**
     * Applies a promo option. Its locator is the confirmed selector with only the
     * option's key replaced, so every value in Promo is selectable through the same code.
     */
    public CreditCardFormPage applyPromo(Promo promo)
    {
        Locator option = page.frameLocator("#snap-midtrans").locator("label[for='" + promo.getKey() + "']");
        click(option, promo.getLabel());
        return this;
    }

    /**
     * Returns the amount shown on the credit card form as plain number text
     * (no currency symbol, no grouping separators, no trailing decimal zeros).
     */
    public String getAmount()
    {
        String rawAmount = getText(amountDisplay, "Payment amount");
        return rawAmount.replaceAll("[^0-9]", "");
    }

    public IssuingBankPage continueToBank()
    {
        click(continueButton, "Continue button");
        return new IssuingBankPage(config);
    }
}
