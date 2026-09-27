package automation.modules.paymentpage.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.modules.paymentpage.PaymentPageData;

public class PaymentSuccessPage extends BasePage
{
    private final Locator amountDisplay;
    private final Locator orderIdDisplay;

    public PaymentSuccessPage(Config config)
    {
        super(config);
        amountDisplay  = page.frameLocator("#snap-midtrans").locator("div.text-headline.large");
        orderIdDisplay = page.frameLocator("#snap-midtrans").locator("div.text-info.small");
        assertPageLoaded(orderIdDisplay);
    }

    /**
     * Captures the displayed amount and order ID before the success page auto-closes.
     */
    public PaymentPageData capturePaymentDetails()
    {
        Log.comment(config, "Capturing payment success details");
        PaymentPageData details = new PaymentPageData();
        details.setOriginalAmount(getText(amountDisplay, "Success page amount"));
        details.setOrderId(getText(orderIdDisplay, "Order ID"));
        return details;
    }
}
