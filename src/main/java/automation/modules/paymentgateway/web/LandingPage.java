package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class LandingPage extends BasePage
{
    private final Locator buyNowButton;
    private final Locator thankYouMessage;

    public LandingPage(Config config)
    {
        super(config);
        buyNowButton = page.locator("a:has-text('Buy Now')");
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
