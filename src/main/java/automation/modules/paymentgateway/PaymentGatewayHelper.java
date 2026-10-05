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
 * Business flow helper for the Midtrans demo payment gateway. Each stage carries one
 * business step, across as many pages as it takes, and returns what that step's checks
 * need. The composed operation runs stages with no check in between, for a test that
 * just wants the full run.
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
     * Build the test's payment data with the test case's own defaults applied.
     */
    public PaymentGatewayData buildPayment()
    {
        return new PaymentGatewayBuilder().build();
    }

    /**
     * Open the cart, fill and submit the checkout details, then open the order details
     * overlay on the payment popup. A stage: returns the name/phone the overlay shows,
     * so the test can verify them against what was filled.
     */
    public OrderDetails startCheckoutAndOpenOrderDetails(PaymentGatewayData payment)
    {
        Log.comment(config, "Navigating to Midtrans demo store: " + config.getRunTimeProperty("paymentgateway.url"));
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("paymentgateway.url"));
        HomePage home = new HomePage(config);
        CartFormPage cartForm = home.clickBuyNow();
        SnapPaymentPage snapPage = cartForm.fillAndSubmitCartDetails(payment);
        snapPage.openOrderDetailsOverlay();
        return new OrderDetails(snapPage.getOrderDetailsName(), snapPage.getOrderDetailsPhone());
    }

    /**
     * Close the order details overlay and read the amount shown at the top of the
     * payment popup. A stage: returns the amount for the test to compare against the
     * amount entered on the cart form.
     */
    public String closeOrderDetailsAndGetAmount()
    {
        SnapPaymentPage snapPage = new SnapPaymentPage(config);
        snapPage.closeOrderDetailsOverlay();
        return snapPage.getAmount();
    }

    /**
     * Choose the payment method, fill the card details and apply the promo. A stage:
     * returns the amount before and after the promo so the test can verify it decreased.
     */
    public PromoResult choosePaymentMethodAndApplyPromo(PaymentMethod method, Promo promo, PaymentGatewayData payment)
    {
        SnapPaymentPage snapPage = new SnapPaymentPage(config);
        CreditCardFormPage cardForm = switch (method)
        {
            case CreditCard -> snapPage.choosePaymentMethod(method);
            default -> throw new UnsupportedOperationException(method.getLabel() + " is not automated yet");
        };
        cardForm.fillCardDetails(payment);
        String amountBefore = cardForm.getAmount();
        String amountAfter = switch (promo)
        {
            case FlashSaleCreditCard ->
            {
                cardForm.applyPromo(promo);
                yield cardForm.getAmount();
            }
            default -> throw new UnsupportedOperationException(promo.getLabel() + " is not automated yet");
        };
        return new PromoResult(amountBefore, amountAfter);
    }

    /**
     * Continue from the credit card form to the Issuing Bank page. A stage: returns
     * the amount shown there so the test can compare it against the amount after promo.
     */
    public String continueToIssuingBank()
    {
        CreditCardFormPage cardForm = new CreditCardFormPage(config);
        IssuingBankPage bankPage = cardForm.continueToBank();
        return bankPage.getAmount();
    }

    /**
     * Enter the bank OTP to finish the payment and capture the success page's amount
     * and order id in the same call, since that page closes almost immediately.
     */
    public PaymentSuccessDetails enterBankOtpAndCompletePayment(PaymentGatewayData payment)
    {
        IssuingBankPage bankPage = new IssuingBankPage(config);
        PaymentSuccessPage successPage = bankPage.enterOtpAndSubmit(payment.getOtp());
        return new PaymentSuccessDetails(successPage.getAmount(), successPage.getOrderId());
    }

    /**
     * Wait for the redirect back to the first page and read the thank-you message.
     */
    public String waitForRedirectHomeAndGetMessage()
    {
        WaitHelper.waitForNetworkIdle(config);
        HomePage home = new HomePage(config);
        return home.getThankYouMessage();
    }

    /**
     * Composed: runs the payment-method/promo, issuing-bank and OTP stages end to end,
     * for a test that only needs the final result, not the intermediate amounts.
     */
    public PaymentSuccessDetails payByCreditCard(Promo promo, PaymentGatewayData payment)
    {
        choosePaymentMethodAndApplyPromo(PaymentMethod.CreditCard, promo, payment);
        continueToIssuingBank();
        return enterBankOtpAndCompletePayment(payment);
    }

    /**
     * Reduces a phone number to comparable digits by stripping everything but digits,
     * then dropping a leading '62' country code or a leading trunk '0' - so a number
     * typed as '081234567890' and one displayed as '+6281234567890' compare equal.
     */
    public String normalizePhoneDigits(String rawPhone)
    {
        String digits = rawPhone.replaceAll("[^0-9]", "");
        if (digits.startsWith("62"))
        {
            return digits.substring(2);
        }
        if (digits.startsWith("0"))
        {
            return digits.substring(1);
        }
        return digits;
    }
}
