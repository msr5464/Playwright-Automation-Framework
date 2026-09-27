package automation.modules.paymentpage.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class DemoHomePage extends BasePage
{
    private final Locator buyNowButton;
    private final Locator thankYouMessage;

    public DemoHomePage(Config config)
    {
        super(config);
        buyNowButton  = page.locator("a:has-text('BUY NOW')");
        thankYouMessage = page.locator(".trans-status.trans-success");
        assertPageLoaded(buyNowButton);
    }

    public CheckoutFormPage clickBuyNow()
    {
        Log.comment(config, "Clicking Buy Now button");
        click(buyNowButton, "Buy Now button");
        return new CheckoutFormPage(config);
    }

    public boolean isThankYouMessageVisible()
    {
        return isElementDisplayed(thankYouMessage);
    }
}
