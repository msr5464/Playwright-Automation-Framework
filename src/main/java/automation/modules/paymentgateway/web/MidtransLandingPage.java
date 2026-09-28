package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class MidtransLandingPage extends BasePage
{
    private final Locator buyNowButton;
    private final Locator thankYouMessage;

    public MidtransLandingPage(Config config)
    {
        super(config);
        buyNowButton = page.locator("a.btn.buy");
        thankYouMessage = page.locator(".notification-wrapper");
        assertPageLoaded(buyNowButton);
    }

    public ShoppingCartFormPage clickBuyNow()
    {
        Log.comment(config, "Clicking Buy Now button to open the shopping cart form");
        click(buyNowButton, "Buy Now button");
        return new ShoppingCartFormPage(config);
    }

    public boolean isThankYouMessageVisible()
    {
        return isElementDisplayed(thankYouMessage);
    }

    public String getThankYouMessageText()
    {
        return getText(thankYouMessage, "Thank you message");
    }
}
