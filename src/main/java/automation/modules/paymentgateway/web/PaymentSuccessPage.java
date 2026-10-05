package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class PaymentSuccessPage extends BasePage
{
    private final Locator successAmount;
    private final Locator successOrderId;

    public PaymentSuccessPage(Config config)
    {
        super(config);
        successAmount = page.frameLocator("#snap-midtrans").locator("div.text-headline.large");
        successOrderId = page.frameLocator("#snap-midtrans").locator("div.text-info.small");
        assertPageLoaded(successAmount);
    }

    public String getSuccessAmount()
    {
        return getText(successAmount, "Success amount");
    }

    public String getSuccessOrderId()
    {
        return getText(successOrderId, "Success order id");
    }
}
