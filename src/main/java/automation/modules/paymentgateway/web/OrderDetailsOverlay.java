package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class OrderDetailsOverlay extends BasePage
{
    private final Locator customerNameLabel;
    private final Locator customerPhoneLabel;
    private final Locator closeButton;

    public OrderDetailsOverlay(Config config)
    {
        super(config);
        customerNameLabel  = page.frameLocator("#snap-midtrans").locator(".order-customer-group div:first-child");
        customerPhoneLabel = page.frameLocator("#snap-midtrans").locator(".order-summary-phone");
        closeButton        = page.frameLocator("#snap-midtrans").locator(".header-modal-content .close-snap-button");
        assertPageLoaded(customerNameLabel);
    }

    public String getCustomerName()
    {
        return getText(customerNameLabel, "Customer name label");
    }

    public String getCustomerPhone()
    {
        return getText(customerPhoneLabel, "Customer phone label");
    }

    public PaymentPage close()
    {
        Log.comment(config, "Closing order details overlay");
        click(closeButton, "Close overlay button");
        return new PaymentPage(config);
    }
}
