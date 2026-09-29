package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class PaymentOverlayPage extends BasePage
{
    private final Locator detailsIcon;
    private final Locator orderDetailsName;
    private final Locator orderDetailsPhone;
    private final Locator closeOverlayButton;
    private final Locator amountDisplay;
    private final Locator creditCardOption;

    public PaymentOverlayPage(Config config)
    {
        super(config);
        detailsIcon = page.frameLocator("#snap-midtrans").locator("div.header-detail-clickable");
        orderDetailsName = page.frameLocator("#snap-midtrans").locator("div.order-customer-group > div:first-child");
        orderDetailsPhone = page.frameLocator("#snap-midtrans").locator("div.order-summary-phone");
        closeOverlayButton = page.frameLocator("#snap-midtrans").locator(".header-modal-content .close-snap-button");
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        creditCardOption = page.frameLocator("#snap-midtrans").locator("a[href='#/credit-card']");
        assertPageLoaded(amountDisplay);
    }

    public void clickDetailsIcon()
    {
        Log.comment(config, "Clicking the order details icon to open the order details overlay");
        click(detailsIcon, "Details icon");
    }

    public String getOrderDetailsName()
    {
        return getText(orderDetailsName, "Order details name");
    }

    public String getOrderDetailsPhone()
    {
        return getText(orderDetailsPhone, "Order details phone");
    }

    public void closeOverlay()
    {
        Log.comment(config, "Closing the order details overlay");
        click(closeOverlayButton, "Close overlay button");
    }

    public String getAmountDisplayed()
    {
        return getText(amountDisplay, "Amount display");
    }

    public CreditCardPaymentPage selectCreditCardMethod()
    {
        Log.comment(config, "Selecting Credit Card as the payment method");
        click(creditCardOption, "Credit Card payment option");
        return new CreditCardPaymentPage(config);
    }
}
