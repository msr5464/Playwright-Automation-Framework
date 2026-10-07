package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;
import automation.modules.paymentgateway.PaymentGatewayHelper;

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
        return PaymentGatewayHelper.toPlainAmount(getText(successAmountDisplay, "Success amount display"));
    }

    public String getOrderId()
    {
        return getText(orderIdDisplay, "Order id display");
    }

    public LandingPage waitForRedirectToLanding()
    {
        Log.comment(config, "Waiting for redirect back to the landing page after payment success");
        WaitHelper.waitForUrl(config, config.getRunTimeProperty("paymentgateway.url"));
        return new LandingPage(config);
    }
}
