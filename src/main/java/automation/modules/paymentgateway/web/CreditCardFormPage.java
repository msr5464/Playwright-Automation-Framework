package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayEnums.PromoCode;

public class CreditCardFormPage extends BasePage
{
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator promoCodeOption;
    private final Locator totalAmountDisplay;
    private final Locator continueButton;

    public CreditCardFormPage(Config config)
    {
        super(config);
        cardNumberField = page.frameLocator("#snap-midtrans").locator("div.card-number-input-container input[type='tel']");
        expiryField = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        promoCodeOption = page.frameLocator("#snap-midtrans").locator("div.promo-button-wrapper");
        totalAmountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        continueButton = page.frameLocator("#snap-midtrans").locator("button.btn.full.primary.btn-theme");
        assertPageLoaded(cardNumberField);
    }

    public CreditCardFormPage fillCardDetails(PaymentGatewayData payment)
    {
        fillText(cardNumberField, payment.getCardNumber(), "Card number field");
        fillText(expiryField, payment.getCardExpiry(), "Card expiry field");
        fillText(cvvField, payment.getCardCvv(), "Card CVV field");
        return this;
    }

    /**
     * Applies the promo code on the credit card form. Its locator is the
     * confirmed selector; no alternative promo codes were recorded, so every
     * value currently routes through the same click.
     */
    public CreditCardFormPage applyPromo(PromoCode promo)
    {
        click(promoCodeOption, promo.getLabel() + " promo");
        return this;
    }

    public String getTotalAmount()
    {
        return getText(totalAmountDisplay, "Total amount display");
    }

    public IssuingBankPage continueToBank()
    {
        click(continueButton, "Continue button");
        return new IssuingBankPage(config);
    }
}
