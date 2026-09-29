package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class PaymentGatewaySuccessPage extends BasePage
{
    private final Locator amountDisplay;
    private final Locator orderIdDisplay;

    public PaymentGatewaySuccessPage(Config config)
    {
        super(config);
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.text-headline.large");
        orderIdDisplay = page.frameLocator("#snap-midtrans").locator("div.text-info.small");
        assertPageLoaded(amountDisplay);
    }

    public String getDisplayedAmount()
    {
        return PaymentGatewayPaymentPopupPage.normalizeAmount(getText(amountDisplay, "Success page amount display"));
    }

    public String getOrderId()
    {
        return getText(orderIdDisplay, "Success page order id display");
    }
}
