package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;

public class SnapPaymentPage extends BasePage
{
    private final Locator detailsIcon;
    private final Locator orderDetailsName;
    private final Locator orderDetailsPhone;
    private final Locator amountDisplay;

    public SnapPaymentPage(Config config)
    {
        super(config);
        detailsIcon = page.frameLocator("#snap-midtrans").locator("div.header-detail-clickable");
        orderDetailsName = page.frameLocator("#snap-midtrans").locator("div.order-customer-group > div:first-child");
        orderDetailsPhone = page.frameLocator("#snap-midtrans").locator("div.order-summary-phone");
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        assertPageLoaded(amountDisplay);
    }

    public SnapPaymentPage openOrderDetailsOverlay()
    {
        click(detailsIcon, "Order details icon");
        return this;
    }

    public String getOrderDetailsName()
    {
        return getText(orderDetailsName, "Order details name");
    }

    public String getOrderDetailsPhone()
    {
        return getText(orderDetailsPhone, "Order details phone");
    }

    public SnapPaymentPage closeOrderDetailsOverlay()
    {
        click(detailsIcon, "Order details icon");
        return this;
    }

    public String getAmount()
    {
        return getText(amountDisplay, "Amount display");
    }

    public CreditCardFormPage choosePaymentMethod(PaymentMethod method)
    {
        Locator option = page.frameLocator("#snap-midtrans").locator("a[href='" + method.getKey() + "']");
        click(option, method.getLabel() + " payment method");
        return new CreditCardFormPage(config);
    }
}
