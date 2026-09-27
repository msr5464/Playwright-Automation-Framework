package automation.modules.paymentgateway;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;
import automation.core.api.ApiHelper;
import automation.modules.paymentgateway.web.CheckoutFormPage;
import automation.modules.paymentgateway.web.CreditCardFormPage;
import automation.modules.paymentgateway.web.IssuingBankPage;
import automation.modules.paymentgateway.web.PaymentGatewayHomePage;
import automation.modules.paymentgateway.web.PaymentPage;
import automation.modules.paymentgateway.web.PaymentSuccessPage;

/**
 * Orchestration helper for the Midtrans demo payment gateway web flows.
 * No REST API endpoints — this module is web-only.
 *
 * Typical usage:
 *   PaymentGatewayHelper helper = new PaymentGatewayHelper(config);
 *   PaymentGatewayData data     = helper.buildDefaultPaymentData();
 *   PaymentPage paymentPage     = helper.fillCheckoutAndProceed(data);
 *   // caller handles overlay verification directly on paymentPage
 *   PaymentGatewayData result   = helper.completePaymentWithCreditCard(paymentPage, data, "Promo Flash Sale (Credit-Card)");
 */
public class PaymentGatewayHelper extends ApiHelper
{
    public PaymentGatewayHelper(Config config)
    {
        super(config, config.getRunTimeProperty("paymentgateway.url"));
    }

    /**
     * Build a PaymentGatewayData instance with sensible defaults via the builder.
     * Call this from the @Test method so no literals appear there.
     */
    public PaymentGatewayData buildDefaultPaymentData()
    {
        return new PaymentGatewayBuilder().withDefaults().build();
    }

    /**
     * Navigate to the Midtrans demo home page, click Buy Now,
     * fill the checkout form with the supplied name and phone, and
     * proceed to the Payment popup.
     *
     * Navigates through: PaymentGatewayHomePage → CheckoutFormPage → PaymentPage
     *
     * @return PaymentPage ready for the next step of the flow
     */
    public PaymentPage fillCheckoutAndProceed(PaymentGatewayData data)
    {
        Log.comment(config, "Navigating to Payment Gateway home page");
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("paymentgateway.url"));
        PaymentGatewayHomePage home = new PaymentGatewayHomePage(config);
        CheckoutFormPage checkout = home.clickBuyNow();
        return checkout.fillAndCheckout(data.getName(), data.getPhone());
    }

    /**
     * From the Payment page, select Credit Card, fill card details, apply the given promo,
     * proceed through the issuing bank OTP step, capture the result from the success screen,
     * and wait for the automatic redirect back to the home page.
     *
     * Navigates through: PaymentPage → CreditCardFormPage → IssuingBankPage → PaymentSuccessPage
     * then waits for the auto-redirect to PaymentGatewayHomePage.
     *
     * The caller is expected to have handled the order-details overlay verification on the
     * PaymentPage before calling this method.
     *
     * The {@code amount} field of the returned object equals the discounted (post-promo) amount
     * shown on the success screen; {@code orderId} is the transaction reference shown there.
     *
     * @param paymentPage PaymentPage already on screen
     * @param data        card number, expiry, CVV and bank OTP to use
     * @param promoText   exact text of the promo option to select from the dropdown
     * @return PaymentGatewayData with amount (discounted) and orderId from the success screen
     */
    public PaymentGatewayData completePaymentWithCreditCard(
            PaymentPage paymentPage, PaymentGatewayData data, String promoText)
    {
        Log.comment(config, "Selecting Credit Card and filling payment details");
        CreditCardFormPage cardPage = paymentPage.selectCreditCard();
        cardPage.enterCardNumber(data.getCardNumber());
        cardPage.enterExpiry(data.getExpiry());
        cardPage.enterCvv(data.getCvv());
        cardPage.selectPromo(promoText);
        String discountedAmount = cardPage.getAmountAfterPromo();

        Log.comment(config, "Proceeding to Issuing Bank page and submitting OTP");
        IssuingBankPage bankPage = cardPage.clickContinue();
        bankPage.enterOtp(data.getBankOtp());
        PaymentSuccessPage successPage = bankPage.submitOtp();

        PaymentGatewayData captured = successPage.capturePaymentDetails();
        if (captured.getAmount() == null)
        {
            captured.setAmount(discountedAmount);
        }

        Log.comment(config, "Waiting for automatic redirect to home page after payment success");
        WaitHelper.waitForNetworkIdle(config);

        return captured;
    }

    /**
     * Parse a localised currency display string (e.g. "Rp 490.909", "IDR 500,000")
     * to a long for numeric comparison. Strips all non-digit characters.
     *
     * @param displayedAmount raw string from the UI
     * @return numeric value, or 0 if the input is blank or null
     */
    public long parseAmount(String displayedAmount)
    {
        if (displayedAmount == null || displayedAmount.isBlank())
        {
            return 0L;
        }
        return Long.parseLong(displayedAmount.replaceAll("[^0-9]", ""));
    }
}
