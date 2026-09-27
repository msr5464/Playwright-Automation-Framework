package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class PaymentGatewayHomePage extends BasePage
{
    private final Locator buyNowButton;
    private final Locator thankYouMessage;

    public PaymentGatewayHomePage(Config config)
    {
        super(config);
        buyNowButton   = page.locator("a.btn.buy");
        thankYouMessage = page.locator(".notification-wrapper");
        assertPageLoaded(buyNowButton, thankYouMessage);
    }

    public CheckoutFormPage clickBuyNow()
    {
        Log.comment(config, "Clicking Buy Now button");
        click(buyNowButton, "Buy Now button");
        return new CheckoutFormPage(config);
    }

    public String getThankYouMessage()
    {
        return getText(thankYouMessage, "Thank you message");
    }

    public boolean isThankYouMessageVisible()
    {
        return isElementDisplayed(thankYouMessage);
    }
}
