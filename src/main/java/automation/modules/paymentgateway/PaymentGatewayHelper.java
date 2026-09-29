package automation.modules.paymentgateway;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.paymentgateway.web.PaymentGatewayCheckoutFormPage;
import automation.modules.paymentgateway.web.PaymentGatewayCreditCardPage;
import automation.modules.paymentgateway.web.PaymentGatewayIssuingBankPage;
import automation.modules.paymentgateway.web.PaymentGatewayLandingPage;
import automation.modules.paymentgateway.web.PaymentGatewayPaymentPopupPage;
import automation.modules.paymentgateway.web.PaymentGatewaySuccessPage;

/**
 * Web orchestration helper for the Midtrans demo checkout flow.
 * This is a public/demo checkout (no application auth), so it extends ApiHelper
 * purely for module consistency - no API endpoints are exercised here since
 * PaymentGatewayApi has no members for this plan.
 *
 * Usage:
 *   PaymentGatewayHelper paymentGateway = new PaymentGatewayHelper(config);
 *   PaymentGatewayData data = paymentGateway.buildCheckoutData();
 *   PaymentGatewayLandingPage landing = paymentGateway.navigateToLandingPage();
 *   PaymentGatewayCheckoutFormPage checkoutForm = landing.clickBuyNow();
 *   PaymentGatewayPaymentPopupPage popup = paymentGateway.fillCheckoutDetailsAndProceed(checkoutForm, data);
 */
public class PaymentGatewayHelper extends ApiHelper
{
    public PaymentGatewayHelper(Config config)
    {
        super(config, config.getRunTimeProperty("paymentgateway.url"));
    }

    /**
     * Build randomized checkout test data. Field shapes (word counts, character
     * kinds) are preserved by PaymentGatewayBuilder's withDefaults() to mirror
     * values validated on the live Midtrans demo checkout flow.
     */
    public PaymentGatewayData buildCheckoutData()
    {
        Log.comment(config, "Building payment checkout test data with default random values");
        return new PaymentGatewayBuilder().build();
    }

    /**
     * Navigate to the Midtrans demo landing page.
     */
    public PaymentGatewayLandingPage navigateToLandingPage()
    {
        String url = config.getRunTimeProperty("paymentgateway.url");
        Log.comment(config, "Navigating to Midtrans demo landing page: " + url);
        BrowserHelper.navigateTo(config, url);
        return new PaymentGatewayLandingPage(config);
    }

    /**
     * Fill all checkout form fields from the prepared test data and submit the
     * shopping cart to open the payment popup. Groups the multi-step data
     * preparation and form-filling sequence that belongs to a single logical
     * scenario step.
     */
    public PaymentGatewayPaymentPopupPage fillCheckoutDetailsAndProceed(PaymentGatewayCheckoutFormPage checkoutForm, PaymentGatewayData data)
    {
        Log.comment(config, "Filling checkout form with amount, name, email, address and phone");
        checkoutForm.fillAmount(data.getAmount());
        checkoutForm.fillName(data.getName());
        checkoutForm.fillEmail(data.getEmail());
        checkoutForm.fillAddress(data.getAddress());
        checkoutForm.fillPhone(data.getPhone());
        return checkoutForm.clickCheckout();
    }

    /**
     * Select Credit Card as the payment method, enter the card details and
     * apply the promo code, stopping short of clicking Continue so the caller
     * can read and assert on the amount displayed after the promo is applied.
     */
    public PaymentGatewayCreditCardPage applyCreditCardDetailsAndPromo(PaymentGatewayPaymentPopupPage paymentPopup, PaymentGatewayData data)
    {
        Log.comment(config, "Selecting Credit Card as payment method and entering card details");
        PaymentGatewayCreditCardPage creditCardPage = paymentPopup.selectCreditCardMethod();
        creditCardPage.fillCardNumber(data.getCardNumber());
        creditCardPage.fillExpiry(data.getCardExpiry());
        creditCardPage.fillCvv(data.getCardCvv());
        creditCardPage.selectPromo(data.getPromoCode());
        return creditCardPage;
    }

    /**
     * Enter the bank OTP and submit it to complete the 3DS challenge, landing
     * on the payment successful page.
     */
    public PaymentGatewaySuccessPage submitOtpAndCompletePayment(PaymentGatewayIssuingBankPage issuingBankPage, String otp)
    {
        Log.comment(config, "Entering bank OTP and submitting to complete the 3DS challenge");
        issuingBankPage.enterOtp(otp);
        return issuingBankPage.submitOtp();
    }

    /**
     * Wait for the automatic redirect back to the Midtrans landing page after
     * a successful payment. PaymentGatewayLandingPage's constructor asserts the
     * page has loaded, which blocks until the redirect completes or the test
     * fails on timeout - no Thread.sleep required.
     */
    public PaymentGatewayLandingPage waitForRedirectToLandingPage()
    {
        Log.comment(config, "Waiting for automatic redirect back to the Midtrans landing page");
        return new PaymentGatewayLandingPage(config);
    }
}
