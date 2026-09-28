package automation.modules.paymentgateway;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.paymentgateway.web.CreditCardFormPage;
import automation.modules.paymentgateway.web.IssuingBankPage;
import automation.modules.paymentgateway.web.OrderDetailsOverlay;
import automation.modules.paymentgateway.web.PaymentGatewayHomePage;
import automation.modules.paymentgateway.web.PaymentPage;
import automation.modules.paymentgateway.web.PaymentSuccessPage;

/**
 * Helper for Midtrans payment gateway web flows.
 * Extends ApiHelper to conform to the standard module structure.
 * No REST API endpoints are consumed — all flows are browser-driven.
 */
public class PaymentGatewayHelper extends ApiHelper
{
    public PaymentGatewayHelper(Config config)
    {
        super(config, config.getRunTimeProperty("paymentgateway.url"));
    }

    // ========== WEB ORCHESTRATION ==========

    /**
     * Navigates to the Midtrans demo home page, clicks Buy Now, fills the checkout form,
     * and returns the Payment popup ready for payment method selection.
     */
    public PaymentPage fillCheckoutAndProceed(PaymentGatewayData data)
    {
        Log.comment(config, "Navigating to Midtrans demo home page");
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("paymentgateway.url"));
        return new PaymentGatewayHomePage(config)
                .clickBuyNow()
                .fillAndCheckout(data.getName(), data.getPhone());
    }

    /**
     * Opens the order details overlay, reads the customer name and phone, closes it,
     * and returns a {@link PaymentGatewayData} with {@code name} and {@code phone} populated.
     */
    public PaymentGatewayData getOrderDetails(PaymentPage paymentPage)
    {
        Log.comment(config, "Opening order details overlay to read customer info");
        OrderDetailsOverlay overlay = paymentPage.clickDetails();
        String name  = overlay.getCustomerName();
        String phone = overlay.getCustomerPhone();
        overlay.close();
        PaymentGatewayData details = new PaymentGatewayData();
        details.setName(name);
        details.setPhone(phone);
        return details;
    }

    /**
     * Selects Credit Card on the Payment page, fills the card details, and applies the given
     * promo. Returns the {@link CreditCardFormPage} with the promo applied, ready to read the
     * discounted amount or click Continue.
     */
    public CreditCardFormPage selectAndFillCreditCard(PaymentPage paymentPage,
                                                      PaymentGatewayData data,
                                                      String promoName)
    {
        Log.comment(config, "Selecting credit card and filling payment details with promo: " + promoName);
        return paymentPage.selectCreditCard()
                .enterCardNumber(data.getCardNumber())
                .enterExpiry(data.getExpiry())
                .enterCvv(data.getCvv())
                .selectPromo(promoName);
    }

    /**
     * Enters the OTP on the Issuing Bank page and submits it.
     * Returns the {@link PaymentSuccessPage} that appears immediately after the OTP is accepted.
     */
    public PaymentSuccessPage completeOtpPayment(IssuingBankPage bankPage, String otp)
    {
        Log.comment(config, "Entering bank OTP and completing payment");
        return bankPage.enterOtp(otp).submitOtp();
    }

    /**
     * Waits for the automatic post-payment redirect back to the home page.
     * The Midtrans success screen auto-closes after a few seconds; constructing
     * {@link PaymentGatewayHomePage} lets {@code assertPageLoaded} absorb the wait.
     */
    public PaymentGatewayHomePage waitForSuccessRedirect()
    {
        Log.comment(config, "Waiting for automatic redirect to home page after payment success");
        return new PaymentGatewayHomePage(config);
    }

    // ========== COMPARISON UTILITIES ==========

    /**
     * Normalises a phone number for equality comparison by stripping all non-digit characters
     * and then removing any leading Indonesian country code ({@code 62}) or trunk digit
     * ({@code 0}).
     * <p>Examples: {@code "+628123456789"} and {@code "08123456789"} both normalise to
     * {@code "8123456789"}.</p>
     */
    public String normalizePhone(String phone)
    {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("62")) return digits.substring(2);
        if (digits.startsWith("0"))  return digits.substring(1);
        return digits;
    }

    /**
     * Parses a displayed amount to its numeric value as a {@code long} (integer IDR units).
     * <p>Handles two formats observed in the Midtrans demo flow:
     * <ul>
     *   <li>Indonesian Rupiah display: {@code "Rp19.000"} — dot is a thousands separator
     *       → {@code 19000}</li>
     *   <li>Issuing bank 3DS display:  {@code "19000.00"} — standard decimal
     *       → {@code 19000}</li>
     * </ul>
     * </p>
     */
    public long parseAmount(String displayedAmount)
    {
        if (displayedAmount.contains("Rp"))
        {
            return Long.parseLong(displayedAmount.replaceAll("[^0-9]", ""));
        }
        return (long) Double.parseDouble(displayedAmount.trim());
    }
}
