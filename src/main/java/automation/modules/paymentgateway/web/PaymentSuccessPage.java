package automation.modules.paymentgateway.web;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.modules.paymentgateway.PaymentGatewayData;

public class PaymentSuccessPage extends BasePage
{
    private final Locator amountDisplay;
    private final Locator orderIdDisplay;

    public PaymentSuccessPage(Config config)
    {
        super(config);
        FrameLocator snapFrame = page.frameLocator("#snap-midtrans");
        amountDisplay  = snapFrame.locator("div.text-headline.large");
        orderIdDisplay = snapFrame.locator("div.text-info.small");
        assertPageLoaded(amountDisplay);
    }

    /**
     * Captures amount and order ID from the success screen immediately before it auto-closes.
     * Returns a {@link PaymentGatewayData} with {@code amount} and {@code orderId} populated.
     */
    public PaymentGatewayData capturePaymentDetails()
    {
        Log.comment(config, "Capturing payment success details before auto-redirect");
        PaymentGatewayData captured = new PaymentGatewayData();
        captured.setAmount(getText(amountDisplay, "Success amount display"));
        captured.setOrderId(getText(orderIdDisplay, "Order ID display"));
        return captured;
    }
}
