package automation.paymentgateway;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.web.CreditCardPaymentPage;
import automation.modules.paymentgateway.web.IssuingBankPage;
import automation.modules.paymentgateway.web.MidtransLandingPage;
import automation.modules.paymentgateway.web.PaymentPopupPage;
import automation.modules.paymentgateway.web.PaymentSuccessPage;
import automation.modules.paymentgateway.web.ShoppingCartFormPage;

public class PaymentGatewayWebTest extends TestBase
{
    @Test(description = "Fill the shopping cart form, verify order details and amount carry through the payment popup, pay via credit card with a promo applied, and verify the final amount, order id and redirect message", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void completeMidtransCreditCardPaymentWithPromo(Config config)
    {
        PaymentGatewayHelper paymentGateway = new PaymentGatewayHelper(config);
        PaymentGatewayData checkoutData = paymentGateway.buildCheckoutData();

        config.logStep("Navigate to https://demo.midtrans.com/");
        MidtransLandingPage landing = paymentGateway.navigateToLandingPage();

        config.logStep("Click Buy Now to open the shopping cart form");
        ShoppingCartFormPage cartForm = landing.clickBuyNow();

        config.logStep("Fill dummy data in all fields and record the entered name, phone, and amount");
        cartForm.fillName(checkoutData.getName());
        cartForm.fillPhone(checkoutData.getPhone());
        cartForm.fillAmount(checkoutData.getAmount());
        cartForm.fillRemainingFieldsWithDummyData();

        config.logStep("Click Checkout");
        PaymentPopupPage paymentPopup = cartForm.clickCheckout();

        config.logStep("On the payment page popup, click the Details icon to open the order details overlay");
        paymentPopup.clickDetailsIcon();

        config.logStep("Verify the name and phone in the order details overlay match the name and phone recorded from the shopping cart form");
        String enteredPhoneDigits = checkoutData.getPhone().replaceFirst("^0", "");
        AssertHelper.assertEquals(config, paymentPopup.getOrderDetailsName(), checkoutData.getName(), "Order details name should match the name entered in the shopping cart form");
        AssertHelper.assertEquals(config, paymentPopup.getOrderDetailsPhone(), enteredPhoneDigits, "Order details phone digits should match the phone entered in the shopping cart form");

        config.logStep("Close the overlay to return to the payment page");
        CreditCardPaymentPage creditCardPage = paymentPopup.closeOverlay();

        config.logStep("Verify the amount on the payment page matches the amount recorded from the shopping cart form");
        AssertHelper.assertEquals(config, paymentPopup.getAmountText(), checkoutData.getAmount(), "Payment popup amount should match the amount entered in the shopping cart form");

        config.logStep("Select the Credit Card payment method");
        creditCardPage.selectCreditCardMethod();

        config.logStep("Enter card number 4111 1111 1111 1111, expiry 01/35, CVV 123");
        creditCardPage.fillCardNumber(checkoutData.getCardNumber());
        creditCardPage.fillExpiry(checkoutData.getCardExpiry());
        creditCardPage.fillCvv(checkoutData.getCardCvv());

        config.logStep("Apply promo Promo Flash Sale (Credit-Card)");
        creditCardPage.selectPromo();

        config.logStep("Verify the amount after applying the promo is less than the amount recorded on the payment page");
        String amountAfterPromo = creditCardPage.getAmountText();
        AssertHelper.assertTrue(config, Double.parseDouble(amountAfterPromo) < Double.parseDouble(paymentPopup.getAmountText()), "Amount after applying the promo should be less than the amount recorded on the payment page");

        config.logStep("Click Continue to proceed to the Issuing Bank page");
        IssuingBankPage issuingBankPage = creditCardPage.clickContinue();

        config.logStep("Verify the amount on the Issuing Bank page matches the amount after applying the promo");
        AssertHelper.assertEquals(config, issuingBankPage.getAmountText(), amountAfterPromo, "Issuing Bank amount should match the amount after applying the promo");

        config.logStep("Enter Bank OTP 112233 and confirm to finish the payment");
        issuingBankPage.fillOtp(checkoutData.getOtp());
        PaymentSuccessPage paymentSuccessPage = issuingBankPage.clickConfirm();

        config.logStep("Immediately capture the amount and order id shown on the payment successful page");
        String successAmount = paymentSuccessPage.getAmountText();
        String orderId = paymentSuccessPage.getOrderId();

        config.logStep("Verify the amount on the payment successful page matches the amount after applying the promo");
        String normalizedSuccessAmount = normalizeAmount(successAmount);
        AssertHelper.assertEquals(config, normalizedSuccessAmount, amountAfterPromo, "Payment successful amount should match the amount after applying the promo");

        config.logStep("Verify the payment successful page displays a non-empty order id");
        AssertHelper.assertTrue(config, orderId != null && !orderId.trim().isEmpty(), "Payment successful page should display a non-empty order id");

        config.logStep("Wait 3-5 seconds for the redirect back to the landing page");
        MidtransLandingPage redirectedLanding = new MidtransLandingPage(config);

        config.logStep("Verify the landing page displays the message 'Thank you for your purchase. Get a nice sleep.'");
        AssertHelper.assertEquals(config, redirectedLanding.getThankYouMessageText(), "Thank you for your purchase. Get a nice sleep.", "Landing page should display the expected thank you message after redirect");
    }

    private static String normalizeAmount(String raw)
    {
        String cleaned = raw.replace("Rp", "").trim();
        cleaned = cleaned.replace(",", "");
        if (cleaned.matches(".*\\.\\d{2}$"))
        {
            cleaned = cleaned.substring(0, cleaned.lastIndexOf('.'));
        }
        else
        {
            cleaned = cleaned.replace(".", "");
        }
        return cleaned;
    }
}
