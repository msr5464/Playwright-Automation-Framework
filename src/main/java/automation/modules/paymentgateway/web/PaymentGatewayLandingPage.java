package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class PaymentGatewayLandingPage extends BasePage
{
    private final Locator buyNowButton;
    private final Locator thankYouMessage;

    public PaymentGatewayLandingPage(Config config)
    {
        super(config);
        buyNowButton = page.locator("button:has-text('Buy Now')");
        thankYouMessage = page.locator("div.trans-status.trans-success");
        assertPageLoaded(buyNowButton);
    }

    public PaymentGatewayCheckoutFormPage clickBuyNow()
    {
        Log.comment(config, "Clicking Buy Now button on the Midtrans demo landing page");
        click(buyNowButton, "Buy Now button");
        return new PaymentGatewayCheckoutFormPage(config);
    }

    public String getThankYouMessageText()
    {
        return getText(thankYouMessage, "Thank you message");
    }
}
