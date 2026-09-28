package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;
import automation.modules.paymentgateway.PaymentGatewayData;

public class CreditCardPaymentPage extends BasePage
{
    private final Locator creditCardMethodOption;
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator promoDropdown;
    private final Locator applyPromoButton;
    private final Locator amountText;
    private final Locator continueButton;

    public CreditCardPaymentPage(Config config)
    {
        super(config);
        creditCardMethodOption = page.frameLocator("#snap-midtrans").locator("div.list-cell:has-text('Credit Card')").first();
        cardNumberField = page.frameLocator("#snap-midtrans").locator("input[placeholder='1234 1234 1234 1234']");
        expiryField = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        promoDropdown = page.frameLocator("#snap-midtrans").locator(".promo-selection");
        applyPromoButton = page.frameLocator("#snap-midtrans").locator("label[for='690']");
        amountText = page.frameLocator("#snap-midtrans").locator(".header-amount");
        continueButton = page.frameLocator("#snap-midtrans").locator("button.btn.full.primary.btn-theme");
        assertPageLoaded(creditCardMethodOption);
    }

    public CreditCardPaymentPage selectCreditCardMethod()
    {
        Log.comment(config, "Selecting Credit Card as the payment method");
        click(creditCardMethodOption, "Credit Card payment method option");
        WaitHelper.waitForElementToBeVisible(config, cardNumberField, "Card number field");
        return this;
    }

    public CreditCardPaymentPage fillCardDetails(PaymentGatewayData data)
    {
        Log.comment(config, "Filling card details - number, expiry, and cvv");
        fillText(cardNumberField, data.getCardNumber(), "Card number field");
        fillText(expiryField, data.getCardExpiry(), "Card expiry field");
        fillText(cvvField, data.getCardCvv(), "Card CVV field");
        return this;
    }

    public String getAmountText()
    {
        return getText(amountText, "Amount text");
    }

    public CreditCardPaymentPage applyPromo(String promoCode)
    {
        Log.comment(config, "Applying promo: " + promoCode);
        click(promoDropdown, "Promo dropdown");
        click(applyPromoButton, "Promo option: " + promoCode);
        WaitHelper.waitForElementToBeVisible(config, amountText, "Amount after promo");
        return this;
    }

    public IssuingBankPage clickContinue()
    {
        Log.comment(config, "Clicking Continue to proceed to the issuing bank page");
        click(continueButton, "Continue button");
        return new IssuingBankPage(config);
    }
}
