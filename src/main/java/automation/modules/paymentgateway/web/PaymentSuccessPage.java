package automation.modules.paymentgateway.web;

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
        amountDisplay  = page.frameLocator("#snap-midtrans").locator("div.text-headline.large");
        orderIdDisplay = page.frameLocator("#snap-midtrans").locator("div.text-info.small");
        assertPageLoaded(amountDisplay);
    }

    /**
     * Captures amount and order ID from the success page before the auto-redirect closes it.
     * Returns a PaymentGatewayData with amount and orderId populated.
     */
    public PaymentGatewayData capturePaymentDetails()
    {
        Log.comment(config, "Capturing payment details from success page");
        String amount  = getText(amountDisplay, "Amount display");
        String orderId = getText(orderIdDisplay, "Order ID display");
        PaymentGatewayData result = new PaymentGatewayData();
        result.setAmount(amount);
        result.setOrderId(orderId);
        return result;
    }
}
