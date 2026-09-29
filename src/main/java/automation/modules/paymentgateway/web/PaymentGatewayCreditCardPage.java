package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class PaymentGatewayCreditCardPage extends BasePage
{
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator promoOption;
    private final Locator amountDisplay;
    private final Locator continueButton;

    public PaymentGatewayCreditCardPage(Config config)
    {
        super(config);
        cardNumberField = page.frameLocator("#snap-midtrans").locator("div.card-number-input-container input[type='tel']");
        expiryField = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        promoOption = page.frameLocator("#snap-midtrans").locator("label[for='690']");
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        continueButton = page.frameLocator("#snap-midtrans").locator("button.btn.full.primary.btn-theme");
        assertPageLoaded(cardNumberField);
    }

    public void fillCardNumber(String cardNumber)
    {
        fillText(cardNumberField, cardNumber, "Card number field");
    }

    public void fillExpiry(String expiry)
    {
        fillText(expiryField, expiry, "Expiry field");
    }

    public void fillCvv(String cvv)
    {
        fillText(cvvField, cvv, "CVV field");
    }

    public void selectPromo(String promoCode)
    {
        Log.comment(config, "Applying promo: " + promoCode);
        click(promoOption, "Promo option: " + promoCode);
    }

    public String getDisplayedAmount()
    {
        return PaymentGatewayPaymentPopupPage.normalizeAmount(getText(amountDisplay, "Credit card amount display"));
    }

    public PaymentGatewayIssuingBankPage clickContinue()
    {
        Log.comment(config, "Clicking Continue button to proceed to the issuing bank page");
        click(continueButton, "Continue button");
        return new PaymentGatewayIssuingBankPage(config);
    }
}
