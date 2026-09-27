package automation.modules.paymentpage;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.paymentpage.web.HomePage;
import automation.modules.paymentpage.web.PaymentPopupPage;
import automation.modules.paymentpage.web.PaymentSuccessPage;

/**
 * Helper for PaymentPage web flows on the Midtrans demo site.
 * Orchestrates HomePage → CheckoutFormPage → PaymentPopupPage.
 */
public class PaymentPageHelper extends ApiHelper
{
    public PaymentPageHelper(Config config)
    {
        super(config, config.getRunTimeProperty("paymentpage.url"));
    }

    /**
     * Navigate to the demo home page, click Buy Now, fill customer checkout details,
     * and submit to open the payment popup.
     * Orchestrates: HomePage + CheckoutFormPage.
     */
    public PaymentPopupPage fillAndSubmitCheckout(PaymentPageData data)
    {
        Log.comment(config, "Navigating to Midtrans demo home page and completing checkout form");
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("paymentpage.url"));
        return new HomePage(config).clickBuyNow()
                                   .fillCheckoutDetails(data)
                                   .clickCheckout();
    }

    /**
     * Capture amount and order ID from the PaymentSuccessPage before it auto-closes.
     */
    public PaymentPageData capturePaymentSuccessDetails(PaymentSuccessPage successPage)
    {
        Log.comment(config, "Capturing payment success details before auto-redirect");
        return successPage.capturePaymentDetails();
    }
}
