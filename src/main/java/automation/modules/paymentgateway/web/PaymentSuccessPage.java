package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class PaymentSuccessPage extends BasePage
{
    private final Locator amountText;
    private final Locator orderIdText;

    public PaymentSuccessPage(Config config)
    {
        super(config);
        amountText = page.frameLocator("#snap-midtrans").locator("div.text-headline.large");
        orderIdText = page.frameLocator("#snap-midtrans").locator("div.text-info.small");
        assertPageLoaded(amountText);
    }

    public String getAmountText()
    {
        return getText(amountText, "Payment successful amount");
    }

    public String getOrderId()
    {
        return getText(orderIdText, "Payment successful order id");
    }
}
