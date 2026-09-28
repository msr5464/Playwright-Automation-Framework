package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class PaymentSuccessPage extends BasePage
{
    private final Locator successAmountText;
    private final Locator successOrderIdText;

    public PaymentSuccessPage(Config config)
    {
        super(config);
        successAmountText = page.frameLocator("#snap-midtrans").locator("div.text-headline.large");
        successOrderIdText = page.frameLocator("#snap-midtrans").locator("div.text-info.small");
        assertPageLoaded(successAmountText);
    }

    public String getSuccessAmountText()
    {
        return getText(successAmountText, "Success amount");
    }

    public String getSuccessOrderId()
    {
        return getText(successOrderIdText, "Success order id");
    }
}
