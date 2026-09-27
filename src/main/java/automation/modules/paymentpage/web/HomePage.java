package automation.modules.paymentpage.web;

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
        buyNowButton    = page.locator("a.btn.buy");
        thankYouMessage = page.locator("div.trans-status.trans-success");
        assertPageLoaded(buyNowButton);
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
}
