package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.PaymentGatewayEnums.PromoCode;

public class CreditCardFormPage extends BasePage
{
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator totalAmountDisplay;
    private final Locator continueButton;

    public CreditCardFormPage(Config config)
    {
        super(config);
        cardNumberField = page.frameLocator("#snap-midtrans").locator("div.card-number-input-container input[type='tel']");
        expiryField = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        totalAmountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        continueButton = page.frameLocator("#snap-midtrans").locator("button.btn.full.primary.btn-theme");
        assertPageLoaded(cardNumberField);
    }

    /**
     * Fill the card number, expiry and CVV fields with the given payment data.
     * Entering these triggers the page's own request to recompute the header
     * amount (e.g. after a card type surcharge), so this waits for the page to
     * settle before any later action or read.
     */
    public CreditCardFormPage fillCardDetails(PaymentGatewayData payment)
    {
        fillText(cardNumberField, payment.getCardNumber(), "Card number field");
        fillText(expiryField, payment.getExpiryDate(), "Expiry field");
        fillText(cvvField, payment.getCvv(), "CVV field");
        WaitHelper.waitForPageToSettle(config);
        return this;
    }

    /**
     * Apply a promo code. The locator is the confirmed selector with only the
     * option's key replaced. Applying a promo recalculates the header amount, so
     * this waits for the page to settle before the amount is read.
     */
    public CreditCardFormPage applyPromo(PromoCode promo)
    {
        Locator option = page.frameLocator("#snap-midtrans").locator("label[for='" + promo.getKey() + "']");
        click(option, promo.getLabel());
        WaitHelper.waitForPageToSettle(config);
        return this;
    }

    public String getTotalAmount()
    {
        return PaymentGatewayHelper.toPlainAmount(getText(totalAmountDisplay, "Total amount"));
    }

    public IssuingBankPage continueToBank()
    {
        click(continueButton, "Continue button");
        return new IssuingBankPage(config);
    }
}
