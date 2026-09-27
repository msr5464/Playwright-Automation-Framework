package automation.modules.paymentpage.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class PaymentPage extends BasePage
{
    private final Locator detailsIcon;
    private final Locator nameDisplay;
    private final Locator phoneDisplay;
    private final Locator creditCardOption;
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator amountDisplay;
    private final Locator promoSelector;
    private final Locator continueButton;

    public PaymentPage(Config config)
    {
        super(config);
        detailsIcon      = page.frameLocator("#snap-midtrans").locator(".header-detail-clickable");
        nameDisplay      = page.frameLocator("#snap-midtrans").locator(".order-customer-group > div:first-child");
        phoneDisplay     = page.frameLocator("#snap-midtrans").locator(".order-summary-phone");
        creditCardOption = page.frameLocator("#snap-midtrans").locator("a[href=\"#/credit-card\"]");
        cardNumberField  = page.frameLocator("#snap-midtrans").locator("input[placeholder=\"1234 1234 1234 1234\"]");
        expiryField      = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField         = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        amountDisplay    = page.frameLocator("#snap-midtrans").locator(".header-amount");
        promoSelector    = page.frameLocator("#snap-midtrans").locator(".promo-selection");
        continueButton   = page.frameLocator("#snap-midtrans").locator("button.btn.full");
        assertPageLoaded(amountDisplay);
    }

    public void clickDetails()
    {
        Log.comment(config, "Clicking Details to expand order details panel");
        click(detailsIcon, "Details icon");
    }

    public String getOrderName()
    {
        return getText(nameDisplay, "Order name display");
    }

    public String getOrderPhone()
    {
        return getText(phoneDisplay, "Order phone display");
    }

    public void selectCreditCard()
    {
        Log.comment(config, "Selecting Credit Card payment option");
        click(creditCardOption, "Credit Card option");
    }

    public void enterCardNumber(String cardNumber)
    {
        Log.comment(config, "Entering card number");
        fillText(cardNumberField, cardNumber, "Card number field");
    }

    public void enterExpiry(String expiry)
    {
        Log.comment(config, "Entering expiry date: " + expiry);
        fillText(expiryField, expiry, "Expiry field");
    }

    public void enterCvv(String cvv)
    {
        Log.comment(config, "Entering CVV");
        fillText(cvvField, cvv, "CVV field");
    }

    public String getAmount()
    {
        return getText(amountDisplay, "Amount display");
    }

    public void selectPromo()
    {
        Log.comment(config, "Selecting Promo Flash Sale");
        click(promoSelector, "Promo selector");
    }

    public IssuingBankPage clickContinue()
    {
        Log.comment(config, "Clicking Continue/Pay Now button");
        click(continueButton, "Continue button");
        return new IssuingBankPage(config);
    }
}
