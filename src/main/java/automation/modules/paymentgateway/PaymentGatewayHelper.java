package automation.modules.paymentgateway;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;
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
 * Flow API for the Midtrans demo payment gateway checkout. Each public method is a
 * stage that constructs the page it starts on and returns what that step's checks
 * need, or a composed operation that chains stages end to end for a test that
 * checks nothing in between.
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
     * Navigate to the store, start checkout with the given payment data, open the
     * order details overlay on the payment popup, and return what it shows.
     */
    public OrderDetails startCheckoutAndOpenOrderDetails(PaymentGatewayData payment)
    {
        String url = config.getRunTimeProperty("paymentgateway.url");
        Log.comment(config, "Navigating to Midtrans demo store: " + url);
        BrowserHelper.navigateTo(config, url);

        HomePage home = new HomePage(config);
        CartFormPage cartForm = home.clickBuyNow();
        SnapPaymentPage snapPayment = cartForm.fillAndSubmitCartDetails(payment);
        snapPayment.openOrderDetailsOverlay();

        String name = snapPayment.getOrderDetailsName();
        String phone = normalizePhone(snapPayment.getOrderDetailsPhone());
        return new OrderDetails(name, phone);
    }

    /**
     * Close the order details overlay and read the amount shown at the top of the
     * payment popup, normalized to plain number text.
     */
    public String closeOrderDetailsAndGetAmount()
    {
        SnapPaymentPage snapPayment = new SnapPaymentPage(config);
        snapPayment.closeOrderDetailsOverlay();
        return normalizeAmount(snapPayment.getAmount());
    }

    /**
     * Choose the payment method, fill its form with the given payment data and apply
     * the promo, returning the amount before and after the promo was applied.
     */
    public PromoResult choosePaymentMethodAndApplyPromo(PaymentMethod method, Promo promo, PaymentGatewayData payment)
    {
        SnapPaymentPage snapPayment = new SnapPaymentPage(config);
        String amountBefore = normalizeAmount(snapPayment.getAmount());

        CreditCardFormPage creditCardForm = snapPayment.choosePaymentMethod(method);
        switch (method)
        {
            case CreditCard -> creditCardForm.fillCardDetails(payment).applyPromo(promo);
            default -> throw new UnsupportedOperationException(method.getLabel() + " is not automated yet");
        }

        String amountAfter = normalizeAmount(creditCardForm.getAmount());
        return new PromoResult(amountBefore, amountAfter);
    }

    /**
     * Continue from the credit card form to the Issuing Bank page and read the
     * amount it shows, normalized to plain number text.
     */
    public String continueToIssuingBank()
    {
        CreditCardFormPage creditCardForm = new CreditCardFormPage(config);
        IssuingBankPage issuingBank = creditCardForm.continueToBank();
        return normalizeAmount(issuingBank.getAmount());
    }

    /**
     * Enter the bank OTP to finish the payment and immediately capture the payment
     * successful page's amount and order id, since that page closes almost
     * immediately after the redirect.
     */
    public PaymentSuccessDetails enterBankOtpAndCompletePayment(PaymentGatewayData payment)
    {
        IssuingBankPage issuingBank = new IssuingBankPage(config);
        PaymentSuccessPage paymentSuccess = issuingBank.enterOtpAndSubmit(payment.getOtp());

        String amount = normalizeAmount(paymentSuccess.getSuccessAmount());
        String orderId = paymentSuccess.getSuccessOrderId();
        return new PaymentSuccessDetails(amount, orderId);
    }

    /**
     * Wait for the redirect back to the first page and read the thank-you message
     * it shows.
     */
    public String waitForRedirectHomeAndGetMessage()
    {
        WaitHelper.waitForNetworkIdle(config);
        HomePage home = new HomePage(config);
        return home.getThankYouMessage();
    }

    /**
     * Complete a full credit-card payment end to end: choose the method and apply
     * the promo, continue to the issuing bank, and enter the OTP to finish. For a
     * test that only needs the final result and checks nothing in between.
     */
    public PaymentSuccessDetails payByCreditCard(Promo promo, PaymentGatewayData payment)
    {
        choosePaymentMethodAndApplyPromo(PaymentMethod.CreditCard, promo, payment);
        continueToIssuingBank();
        return enterBankOtpAndCompletePayment(payment);
    }

    /**
     * Convert a displayed amount ('Rp50.000', '49000.00', 'Rp49.000') to plain
     * number text with no currency symbol, no grouping separators and no trailing
     * decimal zeros, so two displayed amounts can be compared with string equality.
     */
    private String normalizeAmount(String rawAmount)
    {
        String digitsOnly = rawAmount.replaceAll("[^0-9.]", "");
        if (digitsOnly.matches("\\d+\\.\\d{2}"))
        {
            String[] parts = digitsOnly.split("\\.");
            if (parts[1].equals("00"))
            {
                return parts[0];
            }
            String fraction = parts[1].replaceAll("0+$", "");
            return fraction.isEmpty() ? parts[0] : parts[0] + "." + fraction;
        }
        return digitsOnly.replace(".", "");
    }

    /**
     * Convert a displayed phone number ('+6281234567890') to the local trunk-0
     * format ('081234567890') by dropping the country code, so it can be compared
     * with string equality to a phone number typed with a leading trunk 0.
     */
    private String normalizePhone(String rawPhone)
    {
        String digits = rawPhone.replaceAll("[^0-9]", "");
        if (digits.startsWith("62"))
        {
            digits = "0" + digits.substring(2);
        }
        return digits;
    }
}
