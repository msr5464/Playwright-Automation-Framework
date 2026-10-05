package automation.modules.paymentgateway;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;
import automation.modules.paymentgateway.PaymentGatewayEnums.Promo;
import automation.modules.paymentgateway.web.CartFormPage;
import automation.modules.paymentgateway.web.CreditCardFormPage;
import automation.modules.paymentgateway.web.HomePage;
import automation.modules.paymentgateway.web.IssuingBankPage;
import automation.modules.paymentgateway.web.PaymentSuccessPage;
import automation.modules.paymentgateway.web.SnapPaymentPage;
import lombok.Value;

/**
 * Business operations for the Midtrans demo checkout flow. Each stage constructs
 * the page it starts on and returns what that step's checks need; the composed
 * operation runs the stages end to end for tests that don't check the
 * intermediate promo/bank amounts.
 */
public class PaymentGatewayHelper extends ApiHelper
{
    public PaymentGatewayHelper(Config config)
    {
        super(config, config.getRunTimeProperty("paymentgateway.url"));
    }

    @Value
    public static class OrderDetails
    {
        String name;
        String phone;
    }

    @Value
    public static class PromoResult
    {
        String amountBefore;
        String amountAfter;
    }

    @Value
    public static class PaymentSuccessDetails
    {
        String amount;
        String orderId;
    }

    /**
     * Opens the home page, starts checkout with the given payment details and
     * opens the order details overlay on the payment popup.
     */
    public OrderDetails startCheckoutAndOpenOrderDetails(PaymentGatewayData payment)
    {
        String url = config.getRunTimeProperty("paymentgateway.url");
        Log.comment(config, "Navigating to Midtrans demo store: " + url);
        BrowserHelper.navigateTo(config, url);
        HomePage home = new HomePage(config);
        CartFormPage cart = home.clickBuyNow();
        SnapPaymentPage snap = cart.fillAndSubmitCartDetails(payment);
        snap.openOrderDetailsOverlay();
        return new OrderDetails(snap.getOrderDetailsName(), snap.getOrderDetailsPhone());
    }

    /**
     * Closes the order details overlay on the payment popup and returns the
     * amount shown at the top of the page.
     */
    public String closeOrderDetailsAndGetAmount()
    {
        SnapPaymentPage snap = new SnapPaymentPage(config);
        snap.closeOrderDetailsOverlay();
        return snap.getAmount();
    }

    /**
     * Selects the payment method on the popup and, for Credit Card, fills the
     * card details and applies the given promo. Returns the amount before and
     * after the promo is applied.
     */
    public PromoResult choosePaymentMethodAndApplyPromo(PaymentMethod method, Promo promo, PaymentGatewayData payment)
    {
        SnapPaymentPage snap = new SnapPaymentPage(config);
        String amountBefore = snap.getAmount();
        snap.choosePaymentMethod(method);
        switch (method)
        {
            case CreditCard -> {
                CreditCardFormPage creditCardForm = new CreditCardFormPage(config);
                creditCardForm.fillCardDetails(payment);
                creditCardForm.applyPromo(promo);
                String amountAfter = creditCardForm.getAmount();
                return new PromoResult(amountBefore, amountAfter);
            }
            default -> throw new UnsupportedOperationException(method.getLabel() + " is not automated yet");
        }
    }

    /**
     * Continues from the credit card form to the Issuing Bank page and returns
     * the amount shown there.
     */
    public String continueToIssuingBank()
    {
        CreditCardFormPage creditCardForm = new CreditCardFormPage(config);
        IssuingBankPage bank = creditCardForm.continueToBank();
        return bank.getAmount();
    }

    /**
     * Enters the bank OTP to complete the payment and immediately captures the
     * payment successful page's amount and order id, since that page closes
     * almost immediately after the OTP is submitted.
     */
    public PaymentSuccessDetails enterBankOtpAndCompletePayment(PaymentGatewayData payment)
    {
        IssuingBankPage bank = new IssuingBankPage(config);
        PaymentSuccessPage success = bank.enterOtpAndSubmit(payment.getOtp());
        String[] details = success.getSuccessDetails();
        return new PaymentSuccessDetails(details[0], details[1]);
    }

    /**
     * Waits for the redirect back to the home page after a completed payment
     * and returns the thank-you message shown there.
     */
    public String waitForRedirectHomeAndGetMessage()
    {
        HomePage home = new HomePage(config);
        return home.getThankYouMessage();
    }

    /**
     * Composed operation: selects Credit Card as the payment method, applies the
     * given promo, continues to the Issuing Bank page and completes the payment
     * with the OTP, returning the final success details. For a test that just
     * needs the whole credit-card payment run, without checking the
     * intermediate promo/bank amounts.
     */
    public PaymentSuccessDetails payByCreditCard(Promo promo, PaymentGatewayData payment)
    {
        choosePaymentMethodAndApplyPromo(PaymentMethod.CreditCard, promo, payment);
        continueToIssuingBank();
        return enterBankOtpAndCompletePayment(payment);
    }
}
