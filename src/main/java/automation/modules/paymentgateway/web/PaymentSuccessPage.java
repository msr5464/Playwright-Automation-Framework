package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class PaymentSuccessPage extends BasePage
{
    private final Locator successAmountDisplay;
    private final Locator orderIdDisplay;

    public PaymentSuccessPage(Config config)
    {
        super(config);
        successAmountDisplay = page.frameLocator("#snap-midtrans").locator("div.text-headline.large");
        orderIdDisplay = page.frameLocator("#snap-midtrans").locator("div.text-info.small");
        assertPageLoaded(successAmountDisplay);
    }

    public String getSuccessAmount()
    {
        return getText(successAmountDisplay, "Success amount display");
    }

    public String getOrderId()
    {
        return getText(orderIdDisplay, "Order ID display");
    }
}
