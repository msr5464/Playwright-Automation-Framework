package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class PaymentSuccessPage extends BasePage
{
    private final Locator amountDisplay;
    private final Locator orderIdDisplay;

    public PaymentSuccessPage(Config config)
    {
        super(config);
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.text-headline.large");
        orderIdDisplay = page.frameLocator("#snap-midtrans").locator("div.text-info.small");
        assertPageLoaded(amountDisplay);
    }

    public String getAmountDisplayed()
    {
        return getText(amountDisplay, "Amount display");
    }

    public String getOrderId()
    {
        return getText(orderIdDisplay, "Order ID display");
    }
}
