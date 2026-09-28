package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class HomePage extends BasePage
{
    private final Locator buyNowButton;
    private final Locator thankYouMessage;

    public HomePage(Config config)
    {
        super(config);
        buyNowButton = page.locator("a.btn.buy");
        thankYouMessage = page.locator(".notification-wrapper");
        assertPageLoaded(buyNowButton);
    }

    public OrderFormPage clickBuyNow()
    {
        Log.comment(config, "Clicking Buy Now button to open the order form");
        click(buyNowButton, "Buy Now button");
        return new OrderFormPage(config);
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
