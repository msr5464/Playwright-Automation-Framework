package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;
import automation.core.Log;

public class SnapPaymentPage extends BasePage
{
    private final Locator detailsIcon;
    private final Locator orderDetailsName;
    private final Locator orderDetailsPhone;
    private final Locator closeOverlayButton;
    private final Locator amountDisplay;

    public SnapPaymentPage(Config config)
    {
        super(config);
        detailsIcon = page.frameLocator("#snap-midtrans").locator("div.header-detail-clickable");
        orderDetailsName = page.frameLocator("#snap-midtrans").locator("div.order-customer-group > div:first-child");
        orderDetailsPhone = page.frameLocator("#snap-midtrans").locator("div.order-summary-phone");
        closeOverlayButton = page.frameLocator("#snap-midtrans").locator("div.header-modal-content div.close-snap-button.clickable");
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        assertPageLoaded(amountDisplay);
        // The popup's header area keeps re-rendering briefly after the amount first
        // appears (order-summary data still loading), which detaches elements like
        // the details icon out from under an in-flight click. Let that settle before
        // any interaction on this page.
        Log.comment(config, "Waiting for the payment popup to finish loading before interacting with it");
        WaitHelper.waitForNetworkIdle(config);
    }

    public SnapPaymentPage openOrderDetailsOverlay()
    {
        click(detailsIcon, "Order details icon");
        WaitHelper.waitForElementToBeVisible(config, orderDetailsName, "Order details name");
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
        click(closeOverlayButton, "Close order details overlay button");
        return this;
    }

    /**
     * Returns the amount shown at the top of the payment popup as plain number text
     * (no currency symbol, no grouping separators, no trailing decimal zeros).
     */
    public String getAmount()
    {
        String rawAmount = getText(amountDisplay, "Payment amount");
        return rawAmount.replaceAll("[^0-9]", "");
    }

    /**
     * Selects a payment method option. Its locator is the confirmed selector with only
     * the option's key replaced, so every value in PaymentMethod is selectable through
     * the same code. Note: the "GoPayQris" option is shown 2 times on this page, so its
     * locator is not unique there.
     */
    public void choosePaymentMethod(PaymentMethod method)
    {
        Locator option = page.frameLocator("#snap-midtrans").locator("a[href='" + method.getKey() + "']");
        click(option, method.getLabel() + " payment method");
    }
}
