package automation.modules.paymentgateway;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;
import automation.modules.paymentgateway.web.CreditCardPaymentPage;
import automation.modules.paymentgateway.web.IssuingBankPage;
import automation.modules.paymentgateway.web.LandingPage;
import automation.modules.paymentgateway.web.PaymentMethodPage;
import automation.modules.paymentgateway.web.PaymentSuccessPage;
import automation.modules.paymentgateway.web.ShoppingCartFormPage;

/**
 * Helper for the Midtrans payment gateway demo web flow.
 * Holds one public field per page of the module; every action that leaves a page
 * returns the next page object, and the test stores it on the matching field so each
 * step continues from the page the previous step returned.
 *
 * Usage:
 *   PaymentGatewayHelper payments = new PaymentGatewayHelper(config);
 *   payments.paymentMethodPage = payments.checkout(payment);
 *   payments.issuingBankPage = payments.payByCreditCard(payment);
 */
public class PaymentGatewayHelper extends ApiHelper
{
    public LandingPage landingPage;
    public ShoppingCartFormPage shoppingCartFormPage;
    public PaymentMethodPage paymentMethodPage;
    public CreditCardPaymentPage creditCardPaymentPage;
    public IssuingBankPage issuingBankPage;
    public PaymentSuccessPage paymentSuccessPage;

    public PaymentGatewayHelper(Config config)
    {
        super(config, config.getRunTimeProperty("paymentgateway.url"));
    }

    /**
     * Open the midtrans demo, start checkout via Buy Now, fill the customer and
     * amount details and submit the shopping cart form. The entry operation: the
     * only place a page object is constructed, right after navigating. Returns the
     * Payment Method page it lands on.
     */
    public PaymentMethodPage checkout(PaymentGatewayData payment)
    {
        String url = config.getRunTimeProperty("paymentgateway.url");
        Log.comment(config, "Navigating to the Midtrans payment gateway demo: " + url);
        BrowserHelper.navigateTo(config, url);
        landingPage = new LandingPage(config);
        shoppingCartFormPage = landingPage.clickBuyNow();
        shoppingCartFormPage.fillCustomerDetails(payment);
        paymentMethodPage = shoppingCartFormPage.checkout();
        return paymentMethodPage;
    }

    /**
     * Choose Credit Card, fill the card details and promo, and continue to the
     * issuing bank page, continuing from the Payment Method page already stored in
     * paymentMethodPage, for a test that checks nothing in between. Returns the
     * Issuing Bank page it lands on.
     */
    public IssuingBankPage payByCreditCard(PaymentGatewayData payment)
    {
        creditCardPaymentPage = (CreditCardPaymentPage) paymentMethodPage.choosePaymentMethod(PaymentMethod.CreditCard);
        creditCardPaymentPage.fillCardDetails(payment);
        creditCardPaymentPage.applyPromo(payment.getPromoCode());
        issuingBankPage = creditCardPaymentPage.continuePayment();
        return issuingBankPage;
    }

    /**
     * The module's one converter: turns a displayed amount (e.g. "Rp50.000" or
     * "49000.00") into plain number text with no currency symbol, no grouping
     * separators and no trailing decimal zeros, so two displayed amounts from
     * different pages can be compared with a plain string equality assertion.
     */
    public static String toPlainAmount(String shown)
    {
        String cleaned = shown.replace("Rp", "").trim();
        String[] parts = cleaned.split("\\.");
        if (parts.length > 1 && parts[parts.length - 1].length() == 2)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++)
            {
                sb.append(parts[i]);
            }
            String decimal = parts[parts.length - 1];
            if (!decimal.equals("00"))
            {
                sb.append(".").append(decimal);
            }
            return sb.toString();
        }
        return String.join("", parts);
    }
}
