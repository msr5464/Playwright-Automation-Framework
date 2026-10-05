package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class OrderDetailsOverlay extends BasePage
{
    private final Locator orderNameText;
    private final Locator orderPhoneText;
    private final Locator closeOverlayButton;

    public OrderDetailsOverlay(Config config)
    {
        super(config);
        orderNameText = page.frameLocator("#snap-midtrans").locator("div.order-customer-group > div:first-child");
        orderPhoneText = page.frameLocator("#snap-midtrans").locator("div.order-summary-phone");
        closeOverlayButton = page.frameLocator("#snap-midtrans").locator(".header-modal-content .close-snap-button");
        assertPageLoaded(orderNameText);
    }

    public String getOrderName()
    {
        return getText(orderNameText, "Order name");
    }

    public String getOrderPhone()
    {
        return getText(orderPhoneText, "Order phone");
    }

    public PaymentPopupPage close()
    {
        click(closeOverlayButton, "Close overlay button");
        return new PaymentPopupPage(config);
    }
}
