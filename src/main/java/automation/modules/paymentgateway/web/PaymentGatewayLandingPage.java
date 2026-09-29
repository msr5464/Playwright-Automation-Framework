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
        buyNowButton = page.locator("a:has-text('Buy Now')");
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

    /**
     * Normalizes a displayed message by stripping all whitespace and lowercasing,
     * so that a rendering difference in inter-sentence spacing (e.g. no space
     * after a period) or letter case does not affect comparison of the message text.
     */
    public static String normalizeMessage(String raw)
    {
        if (raw == null)
        {
            return null;
        }
        return raw.replaceAll("\\s+", "").toLowerCase();
    }
}
