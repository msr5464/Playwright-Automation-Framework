package automation.modules.paymentpage;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.paymentpage.web.CheckoutFormPage;
import automation.modules.paymentpage.web.DemoHomePage;
import automation.modules.paymentpage.web.IssuingBankPage;
import automation.modules.paymentpage.web.PaymentPage;
import automation.modules.paymentpage.web.PaymentSuccessPage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper for PaymentPage (Midtrans demo) web flows.
 * All page orchestration lives here; the @Test method stays declarative.
 */
public class PaymentPageHelper extends ApiHelper
{
    public PaymentPageHelper(Config config)
    {
        super(config, config.getRunTimeProperty("paymentpage.url"));
    }

    /**
     * Navigate through the full credit-card + Promo Flash Sale payment flow on the Midtrans demo site.
     *
     * Returns a map of values captured during the flow for test assertions:
     *   orderName, orderPhone, amountBeforePromo, amountAfterPromo,
     *   issuingBankAmount, successAmount, successOrderId, thankYouVisible
     */
    public Map<String, String> completeCreditCardPaymentWithPromo(PaymentPageData data)
    {
        Log.comment(config, "Navigating to Midtrans demo site");
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("paymentpage.url"));

        CheckoutFormPage checkout = new DemoHomePage(config).clickBuyNow();

        Log.comment(config, "Filling checkout form with customer details");
        checkout.fillForm(data);
        PaymentPage payment = checkout.clickCheckout();

        Log.comment(config, "Expanding order details panel and capturing customer details");
        payment.clickDetails();
        String orderName  = payment.getOrderName();
        String orderPhone = payment.getOrderPhone();

        Log.comment(config, "Selecting Credit Card and entering card details");
        payment.selectCreditCard();
        payment.enterCardNumber("4111111111111111");
        payment.enterExpiry("01/35");
        payment.enterCvv("123");

        String amountBeforePromo = payment.getAmount();
        Log.comment(config, "Amount before promo: " + amountBeforePromo);

        payment.selectPromo();
        String amountAfterPromo = payment.getAmount();
        Log.comment(config, "Amount after promo: " + amountAfterPromo);

        IssuingBankPage issuingBank    = payment.clickContinue();
        String          issuingBankAmount = issuingBank.getAmount();

        Log.comment(config, "Submitting OTP on issuing bank page");
        issuingBank.enterOtp("112233");
        PaymentSuccessPage success = issuingBank.submit();

        String successAmount  = success.getAmount();
        String successOrderId = success.getOrderId();
        Log.comment(config, "Payment success — amount: " + successAmount + ", order ID: " + successOrderId);

        Log.comment(config, "Waiting for automatic redirect back to home page");
        DemoHomePage returnedHome  = new DemoHomePage(config);
        boolean      thankYouVisible = returnedHome.isThankYouMessageVisible();

        Map<String, String> result = new LinkedHashMap<>();
        result.put("orderName",         orderName);
        result.put("orderPhone",        orderPhone);
        result.put("amountBeforePromo", amountBeforePromo);
        result.put("amountAfterPromo",  amountAfterPromo);
        result.put("issuingBankAmount", issuingBankAmount);
        result.put("successAmount",     successAmount);
        result.put("successOrderId",    successOrderId);
        result.put("thankYouVisible",   String.valueOf(thankYouVisible));
        return result;
    }
}
