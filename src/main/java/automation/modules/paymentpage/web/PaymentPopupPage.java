package automation.modules.paymentpage.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class PaymentPopupPage extends BasePage
{
    private final Locator detailsIcon;
    private final Locator orderDetailsCustomerName;
    private final Locator orderDetailsEmail;
    private final Locator orderDetailsPhone;
    private final Locator creditCardOption;
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator promoDropdown;
    private final Locator amountDisplay;
    private final Locator continueButton;

    public PaymentPopupPage(Config config)
    {
        super(config);
        detailsIcon              = page.frameLocator("#snap-midtrans").locator(".header-detail-clickable");
        orderDetailsCustomerName = page.frameLocator("#snap-midtrans").locator(".order-customer-group div:first-child");
        orderDetailsEmail        = page.frameLocator("#snap-midtrans").locator(".order-summary-email");
        orderDetailsPhone        = page.frameLocator("#snap-midtrans").locator(".order-summary-phone");
        creditCardOption         = page.frameLocator("#snap-midtrans").locator("#credit_card a[data-testid='list-item']");
        cardNumberField          = page.frameLocator("#snap-midtrans").locator("input[placeholder='1234 1234 1234 1234']");
        expiryField              = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField                 = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        promoDropdown            = page.frameLocator("#snap-midtrans").locator(".promo-selection");
        amountDisplay            = page.frameLocator("#snap-midtrans").locator("div.text-headline.large");
        continueButton           = page.frameLocator("#snap-midtrans").locator("button.btn.full.primary");
        assertPageLoaded(amountDisplay);
    }

    public PaymentPopupPage clickDetailsIcon()
    {
        Log.comment(config, "Clicking order details icon");
        click(detailsIcon, "Details icon");
        return this;
    }

    public String getOrderDetailsCustomerName()
    {
        return getText(orderDetailsCustomerName, "Order details customer name");
    }

    public String getOrderDetailsEmail()
    {
        return getText(orderDetailsEmail, "Order details email");
    }

    public String getOrderDetailsPhone()
    {
        return getText(orderDetailsPhone, "Order details phone");
    }

    public PaymentPopupPage selectCreditCard()
    {
        Log.comment(config, "Selecting Credit Card payment method");
        click(creditCardOption, "Credit Card option");
        return this;
    }

    public PaymentPopupPage fillCardNumber(String cardNumber)
    {
        Log.comment(config, "Filling card number");
        typeText(cardNumberField, cardNumber, "Card number field");
        return this;
    }

    public PaymentPopupPage fillExpiry(String expiry)
    {
        Log.comment(config, "Filling card expiry");
        fillText(expiryField, expiry, "Expiry field");
        return this;
    }

    public PaymentPopupPage fillCvv(String cvv)
    {
        Log.comment(config, "Filling CVV");
        fillText(cvvField, cvv, "CVV field");
        return this;
    }

    public PaymentPopupPage selectPromo(String promoName)
    {
        Log.comment(config, "Opening promo dropdown");
        click(promoDropdown, "Promo dropdown");
        Locator promoOption = page.frameLocator("#snap-midtrans")
            .locator("li:has-text(\"" + promoName + "\")");
        click(promoOption, "Promo option: " + promoName);
        return this;
    }

    public String getAmountDisplay()
    {
        return getText(amountDisplay, "Amount display");
    }

    public IssuingBankPage clickContinue()
    {
        Log.comment(config, "Clicking Continue button");
        click(continueButton, "Continue button");
        return new IssuingBankPage(config);
    }
}
