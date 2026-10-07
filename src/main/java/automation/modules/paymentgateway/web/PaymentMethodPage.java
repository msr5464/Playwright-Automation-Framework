package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;

public class PaymentMethodPage extends BasePage
{
    private final Locator detailsIcon;
    private final Locator orderDetailsName;
    private final Locator orderDetailsPhone;
    private final Locator closeOverlayButton;
    private final Locator totalAmountDisplay;

    public PaymentMethodPage(Config config)
    {
        super(config);
        detailsIcon = page.frameLocator("#snap-midtrans").locator("div.header-detail-clickable");
        orderDetailsName = page.frameLocator("#snap-midtrans").locator("div.order-customer-group div:has-text('Mukesh Rajput')");
        orderDetailsPhone = page.frameLocator("#snap-midtrans").locator("div.order-summary-phone");
        closeOverlayButton = page.frameLocator("#snap-midtrans").locator("div.header-modal-content div.close-snap-button.clickable");
        totalAmountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        assertPageLoaded(totalAmountDisplay);
    }

    public PaymentMethodPage openOrderDetails()
    {
        Log.comment(config, "Opening the order details overlay");
        click(detailsIcon, "Details icon");
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

    public PaymentMethodPage closeOrderDetails()
    {
        Log.comment(config, "Closing the order details overlay");
        click(closeOverlayButton, "Close overlay button");
        return this;
    }

    public String getTotalAmount()
    {
        return PaymentGatewayHelper.toPlainAmount(getText(totalAmountDisplay, "Total amount display"));
    }

    public BasePage choosePaymentMethod(PaymentMethod paymentMethod)
    {
        Locator paymentMethodOption = page.frameLocator("#snap-midtrans").locator("a[href='" + paymentMethod.getKey() + "']");
        Log.comment(config, "Choosing payment method: " + paymentMethod.getLabel());
        click(paymentMethodOption, paymentMethod.getLabel() + " payment method");
        return switch (paymentMethod)
        {
            case CreditCard -> new CreditCardPaymentPage(config);
            default -> throw new UnsupportedOperationException(paymentMethod.getLabel() + " is not automated yet");
        };
    }
}
