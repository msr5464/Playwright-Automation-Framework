package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
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

    public String getAmount()
    {
        return PaymentGatewayHelper.toPlainAmount(getText(successAmountDisplay, "Payment successful amount"));
    }

    public String getOrderId()
    {
        return getText(orderIdDisplay, "Order id");
    }

    /**
     * The success overlay closes on its own and the app redirects back to the
     * store home page. Wait for the overlay to disappear, then for the home
     * page URL, before constructing the page the test lands on.
     */
    public StoreHomePage waitForRedirectToHome()
    {
        WaitHelper.waitForElementToBeHidden(config, successAmountDisplay, "Payment successful overlay");
        WaitHelper.waitForUrl(config, config.getRunTimeProperty("paymentgateway.url"));
        return new StoreHomePage(config);
    }
}
