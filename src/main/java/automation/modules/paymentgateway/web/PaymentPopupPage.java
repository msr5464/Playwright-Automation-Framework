package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;

public class PaymentPopupPage extends BasePage
{
    private final Locator detailsIcon;
    private final Locator orderDetailsOverlay;
    private final Locator overlayCustomerName;
    private final Locator overlayCustomerPhone;
    private final Locator closeOverlayButton;
    private final Locator topAmountText;

    public PaymentPopupPage(Config config)
    {
        super(config);
        detailsIcon = page.frameLocator("#snap-midtrans").locator(".header-detail-clickable");
        orderDetailsOverlay = page.frameLocator("#snap-midtrans").locator(".header-modal-content");
        overlayCustomerName = page.frameLocator("#snap-midtrans").locator(".order-customer-group div:first-child");
        overlayCustomerPhone = page.frameLocator("#snap-midtrans").locator(".order-summary-phone");
        closeOverlayButton = page.frameLocator("#snap-midtrans").locator(".header-modal-content .close-snap-button");
        topAmountText = page.frameLocator("#snap-midtrans").locator(".header-amount");
        assertPageLoaded(topAmountText);
    }

    public PaymentPopupPage openOrderDetailsOverlay()
    {
        Log.comment(config, "Clicking details icon to open order details overlay");
        click(detailsIcon, "Order details icon");
        WaitHelper.waitForElementToBeVisible(config, orderDetailsOverlay, "Order details overlay");
        return this;
    }

    public String getOverlayCustomerName()
    {
        return getText(overlayCustomerName, "Overlay customer name");
    }

    public String getOverlayCustomerPhone()
    {
        return getText(overlayCustomerPhone, "Overlay customer phone");
    }

    public PaymentPopupPage closeOverlay()
    {
        Log.comment(config, "Closing order details overlay");
        click(closeOverlayButton, "Close overlay button");
        WaitHelper.waitForElementToBeHidden(config, orderDetailsOverlay, "Order details overlay");
        return this;
    }

    public String getTopAmountText()
    {
        return getText(topAmountText, "Top amount text");
    }
}
