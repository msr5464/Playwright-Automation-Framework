package automation.modules.paymentpage.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class PaymentSuccessPage extends BasePage
{
    private final Locator amountDisplay;
    private final Locator orderIdDisplay;

    public PaymentSuccessPage(Config config)
    {
        super(config);
        amountDisplay  = page.frameLocator("#snap-midtrans").locator("div.text-headline.large");
        orderIdDisplay = page.frameLocator("#snap-midtrans").locator("div.text-info.small");
        assertPageLoaded(amountDisplay);
    }

    public String getAmount()
    {
        return getText(amountDisplay, "Success amount display");
    }

    public String getOrderId()
    {
        return getText(orderIdDisplay, "Order ID display");
    }
}
