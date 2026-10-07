package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayHelper;

public class CreditCardPaymentPage extends BasePage
{
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator promoOption;
    private final Locator totalDisplay;
    private final Locator continueButton;

    public CreditCardPaymentPage(Config config)
    {
        super(config);
        cardNumberField = page.frameLocator("#snap-midtrans").locator("div.card-number-input-container input[type='tel']");
        expiryField = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        promoOption = page.frameLocator("#snap-midtrans").locator("label[for='690']");
        totalDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        continueButton = page.frameLocator("#snap-midtrans").locator("button.btn.full.primary.btn-theme");
        assertPageLoaded(cardNumberField);
    }

    public CreditCardPaymentPage fillCardDetails(PaymentGatewayData payment)
    {
        Log.comment(config, "Filling card number, expiry and CVV");
        fillText(cardNumberField, payment.getCardNumber(), "Card number field");
        fillText(expiryField, payment.getCardExpiry(), "Expiry field");
        fillText(cvvField, payment.getCardCvv(), "CVV field");
        return this;
    }

    public CreditCardPaymentPage applyPromo(String promoCode)
    {
        Log.comment(config, "Applying promo: " + promoCode);
        click(promoOption, "Promo option: " + promoCode);
        WaitHelper.waitForPageToSettle(config);
        return this;
    }

    public String getTotal()
    {
        return PaymentGatewayHelper.toPlainAmount(getText(totalDisplay, "Total display"));
    }

    public IssuingBankPage continuePayment()
    {
        Log.comment(config, "Clicking Continue to proceed to the issuing bank page");
        click(continueButton, "Continue button");
        return new IssuingBankPage(config);
    }
}
