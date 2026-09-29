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
        buyNowButton = page.locator("button:has-text('Buy Now')");
        thankYouMessage = page.locator("div.trans-status.trans-success");
        assertPageLoaded(buyNowButton);
    }

    public ShoppingCartFormPage clickBuyNow()
    {
        Log.comment(config, "Clicking Buy Now button on Midtrans landing page");
        click(buyNowButton, "Buy Now button");
        return new ShoppingCartFormPage(config);
    }

    public String getThankYouMessageText()
    {
        return getText(thankYouMessage, "Thank you message");
    }
}
