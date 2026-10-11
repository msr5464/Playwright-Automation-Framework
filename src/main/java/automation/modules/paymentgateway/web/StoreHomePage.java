package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class StoreHomePage extends BasePage
{
    private final Locator buyNowButton;
    private final Locator thankYouMessage;

    public StoreHomePage(Config config)
    {
        super(config);
        buyNowButton = page.locator("a.btn.buy");
        thankYouMessage = page.locator("div.trans-status.trans-success");
        assertPageLoaded(buyNowButton);
    }

    public CartFormPage clickBuyNow()
    {
        click(buyNowButton, "Buy Now button");
        return new CartFormPage(config);
    }

    public String getThankYouMessage()
    {
        return getText(thankYouMessage, "Thank you message");
    }
}
