package automation.modules.paymentgateway;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.paymentgateway.web.MidtransLandingPage;

/**
 * Helper for the Midtrans demo payment gateway web flow.
 * This module is web-only (guest checkout on demo.midtrans.com) — there is no
 * backing API, so the ApiHelper base URL is the site's own base URL purely to
 * satisfy the framework's Helper contract; no execute()/executeRaw() calls are made.
 *
 * Usage:
 *   PaymentGatewayHelper paymentGateway = new PaymentGatewayHelper(config);
 *   PaymentGatewayData checkoutData = paymentGateway.buildCheckoutData();
 *   MidtransLandingPage landing = paymentGateway.navigateToLandingPage();
 */
public class PaymentGatewayHelper extends ApiHelper
{
    public PaymentGatewayHelper(Config config)
    {
        super(config, config.getRunTimeProperty("paymentgateway.url"));
    }

    /**
     * Navigates to the Midtrans demo landing page.
     */
    public MidtransLandingPage navigateToLandingPage()
    {
        String url = config.getRunTimeProperty("paymentgateway.url");
        Log.comment(config, "Navigating to Midtrans demo landing page: " + url);
        BrowserHelper.navigateTo(config, url);
        return new MidtransLandingPage(config);
    }

    /**
     * Builds checkout test data (name, phone, amount, card details, promo code, OTP)
     * using sensible module defaults, keeping the same shape validated against the
     * live Midtrans demo form (two-word name, single-word phone/amount/card values).
     */
    public PaymentGatewayData buildCheckoutData()
    {
        Log.comment(config, "Building checkout test data with default card, promo and OTP values");
        return new PaymentGatewayBuilder().build();
    }
}
