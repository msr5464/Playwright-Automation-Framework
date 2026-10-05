package automation.modules.paymentgateway;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.EmailHelper;
import automation.core.Log;
import automation.core.WaitHelper;
import automation.core.api.ApiHelper;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;
import automation.modules.paymentgateway.PaymentGatewayEnums.PromoCode;
import automation.modules.paymentgateway.web.CartFormPage;
import automation.modules.paymentgateway.web.CreditCardFormPage;
import automation.modules.paymentgateway.web.IssuingBankPage;
import automation.modules.paymentgateway.web.LandingPage;
import automation.modules.paymentgateway.web.OrderDetailsOverlay;
import automation.modules.paymentgateway.web.PaymentPopupPage;
import automation.modules.paymentgateway.web.PaymentSuccessPage;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Business operations for the Midtrans demo payment gateway flow.
 * Each stage constructs the page it starts on and returns what the next
 * check needs; the composed operation runs every stage end to end for a
 * test that checks nothing in between.
 */
public class PaymentGatewayHelper extends ApiHelper
{
    public PaymentGatewayHelper(Config config)
    {
        super(config);
    }

    /**
     * Reads test data defaults and builds the payment payload for a test's
     * one setup line.
     */
    public PaymentGatewayData buildPayment()
    {
        return new PaymentGatewayBuilder().build();
    }

    @Value
    public static class OrderDetails
    {
        String name;
        String phone;
    }

    @Value
    public static class PaymentReceipt
    {
        String amount;
        String orderId;
    }

    /**
     * Opens the landing page, starts checkout and submits the cart form.
     * A stage: ends on the payment popup, so the test checks the popup next.
     */
    public void checkout(PaymentGatewayData payment)
    {
        String url = config.getRunTimeProperty("paymentgateway.url");
        Log.comment(config, "Navigating to payment gateway demo: " + url);
        BrowserHelper.navigateTo(config, url);
        LandingPage landing = new LandingPage(config);
        CartFormPage cartForm = landing.clickBuyNow();
        cartForm.fillCartDetails(payment).submitCheckout();
    }

    /**
     * Opens the order details overlay on the payment popup and reads the
     * name and phone shown there.
     */
    public OrderDetails viewOrderDetails()
    {
        PaymentPopupPage popup = new PaymentPopupPage(config);
        OrderDetailsOverlay overlay = popup.openOrderDetails();
        return new OrderDetails(overlay.getOrderName(), overlay.getOrderPhone());
    }

    /**
     * Closes the order details overlay and reads the amount shown on the
     * payment popup, normalized to plain number text.
     */
    public String closeOrderDetailsAndGetAmount()
    {
        OrderDetailsOverlay overlay = new OrderDetailsOverlay(config);
        PaymentPopupPage popup = overlay.close();
        return normalizeAmount(popup.getDisplayedAmount());
    }

    /**
     * Chooses the credit card payment method, fills the card details, applies
     * the promo code and reads the total amount after the promo, normalized
     * to plain number text.
     */
    public String enterCreditCardDetails(PaymentGatewayData payment, PromoCode promo)
    {
        PaymentPopupPage popup = new PaymentPopupPage(config);
        popup.choosePaymentMethod(PaymentMethod.CreditCard);
        CreditCardFormPage cardForm = new CreditCardFormPage(config);
        cardForm.fillCardDetails(payment).applyPromo(promo);
        return normalizeAmount(cardForm.getTotalAmount());
    }

    /**
     * Continues from the credit card form to the issuing bank page and reads
     * the amount shown there, normalized to plain number text.
     */
    public String continueToIssuingBank()
    {
        CreditCardFormPage cardForm = new CreditCardFormPage(config);
        IssuingBankPage bankPage = cardForm.continueToBank();
        return normalizeAmount(bankPage.getBankAmount());
    }

    /**
     * Enters the bank OTP to finish the payment and immediately captures the
     * amount and order id shown on the payment successful page, since that
     * page closes instantly.
     */
    public PaymentReceipt confirmBankOtp()
    {
        IssuingBankPage bankPage = new IssuingBankPage(config);
        PaymentSuccessPage successPage = bankPage.enterOtp(EmailHelper.generateStaticOTP());
        return new PaymentReceipt(successPage.getSuccessAmount(), successPage.getOrderId());
    }

    /**
     * Waits for the redirect back to the landing page and reads the thank
     * you message shown there.
     */
    public String verifyRedirectToThankYouPage()
    {
        String url = config.getRunTimeProperty("paymentgateway.url");
        WaitHelper.waitForUrl(config, url);
        LandingPage landing = new LandingPage(config);
        return landing.getThankYouMessage();
    }

    /**
     * Runs the credit card payment stages end to end: enters card details
     * with the given promo, continues to the issuing bank, confirms the OTP
     * and waits for the thank-you redirect. For a test that just needs the
     * whole run and reads only the final message.
     */
    public String payWithCreditCard(PaymentGatewayData payment, PromoCode promo)
    {
        enterCreditCardDetails(payment, promo);
        continueToIssuingBank();
        confirmBankOtp();
        return verifyRedirectToThankYouPage();
    }

    /**
     * Strips currency symbols, grouping separators and trailing decimal
     * zeros from a displayed amount, returning plain number text so two
     * differently formatted displays of the same amount can be compared
     * with a string equality assertion.
     */
    private String normalizeAmount(String rawAmount)
    {
        String digitsAndDot = rawAmount.replaceAll("[^0-9.]", "");
        if (digitsAndDot.contains("."))
        {
            int lastDot = digitsAndDot.lastIndexOf(".");
            String afterDot = digitsAndDot.substring(lastDot + 1);
            if (afterDot.length() == 3)
            {
                digitsAndDot = digitsAndDot.replace(".", "");
            }
            else
            {
                BigDecimal value = new BigDecimal(digitsAndDot);
                digitsAndDot = value.stripTrailingZeros().toPlainString();
                if (digitsAndDot.contains("."))
                {
                    digitsAndDot = digitsAndDot.substring(0, digitsAndDot.indexOf("."));
                }
            }
        }
        return digitsAndDot;
    }
}
