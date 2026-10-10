package automation.modules.paymentgateway;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;
import automation.modules.paymentgateway.PaymentGatewayEnums.PromoCode;
import automation.modules.paymentgateway.web.CartFormPage;
import automation.modules.paymentgateway.web.CreditCardFormPage;
import automation.modules.paymentgateway.web.IssuingBankPage;
import automation.modules.paymentgateway.web.PaymentMethodPage;
import automation.modules.paymentgateway.web.PaymentSuccessPage;
import automation.modules.paymentgateway.web.StoreHomePage;

/**
 * Helper for the Midtrans demo payment gateway web flow. Holds one public field per
 * page of the module. Every action that leaves a page returns the next page object,
 * and the test stores it on the matching field, so each step continues from the page
 * the previous step returned.
 *
 * Usage:
 *   PaymentGatewayHelper gateway = new PaymentGatewayHelper(config);
 *   gateway.paymentMethodPage = gateway.startCheckout(payment);
 *   gateway.paymentSuccessPage = gateway.makePayment(PaymentMethod.CreditCard, payment, PromoCode.FlashSaleCreditCard, payment.getBankOtp());
 */
public class PaymentGatewayHelper extends ApiHelper
{
    public StoreHomePage storeHomePage;
    public CartFormPage cartFormPage;
    public PaymentMethodPage paymentMethodPage;
    public CreditCardFormPage creditCardFormPage;
    public IssuingBankPage issuingBankPage;
    public PaymentSuccessPage paymentSuccessPage;

    public PaymentGatewayHelper(Config config)
    {
        super(config, config.getRunTimeProperty("paymentgateway.url"));
    }

    /**
     * Open the Midtrans demo store, click Buy Now, fill the checkout form with the
     * given payment data, and submit checkout. The entry operation: the only place a
     * page object is constructed, right after navigating. Returns the Payment Method
     * page it lands on.
     */
    public PaymentMethodPage startCheckout(PaymentGatewayData payment)
    {
        String url = config.getRunTimeProperty("paymentgateway.url");
        Log.comment(config, "Navigating to Midtrans demo store: " + url);
        BrowserHelper.navigateTo(config, url);
        storeHomePage = new StoreHomePage(config);
        cartFormPage = storeHomePage.clickBuyNow();
        paymentMethodPage = cartFormPage.fillDetailsAndCheckout(payment);
        return paymentMethodPage;
    }

    /**
     * Choose the payment method, fill card details (for credit card), apply the given
     * promo, continue to the issuing bank, and finish with the OTP. Continues from the
     * pages already in the Helper's fields, storing each page it passes through.
     * Returns the Payment Successful page it lands on.
     */
    public PaymentSuccessPage makePayment(PaymentMethod method, PaymentGatewayData payment, PromoCode promo, String otp)
    {
        switch (method)
        {
            case CreditCard ->
            {
                creditCardFormPage = (CreditCardFormPage) paymentMethodPage.choosePaymentMethod(method);
                creditCardFormPage.fillCardDetails(payment);
                creditCardFormPage.applyPromo(promo);
                issuingBankPage = creditCardFormPage.continueToBank();
                paymentSuccessPage = issuingBankPage.enterOtpAndFinish(otp);
            }
            default -> throw new UnsupportedOperationException(method.getLabel() + " is not automated yet");
        }
        return paymentSuccessPage;
    }

    /**
     * Convert a displayed amount (e.g. "Rp50.000" or "49000.00") into plain number
     * text with no currency symbol, no grouping separators and no trailing decimal
     * zeros, so two amounts shown in different formats can be compared as strings.
     */
    public static String toPlainAmount(String shown)
    {
        String digitsOnly = shown.replaceAll("[^0-9.]", "");
        if (digitsOnly.contains("."))
        {
            digitsOnly = digitsOnly.replaceAll("\\.0+$", "");
            digitsOnly = digitsOnly.replaceAll("(\\.\\d*?)0+$", "$1");
            digitsOnly = digitsOnly.replaceAll("\\.$", "");
        }
        return digitsOnly;
    }
}
