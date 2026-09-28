package automation.paymentgateway;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.WaitHelper;
import automation.core.Enums.*;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.web.CreditCardPaymentPage;
import automation.modules.paymentgateway.web.HomePage;
import automation.modules.paymentgateway.web.IssuingBankPage;
import automation.modules.paymentgateway.web.OrderFormPage;
import automation.modules.paymentgateway.web.PaymentPopupPage;
import automation.modules.paymentgateway.web.PaymentSuccessPage;

public class PaymentGatewayWebTest extends TestBase
{

    @Test(description = "Complete a Midtrans demo checkout with Credit Card payment, validating order details, amount consistency across the flow, promo discount, and the final success/thank-you state",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void completeCreditCardPaymentFlow(Config config)
    {
        PaymentGatewayHelper paymentGateway = new PaymentGatewayHelper(config);
        PaymentGatewayData orderData = paymentGateway.buildOrderData();

        config.logStep("Navigate to HomePage at the base URL");
        HomePage home = paymentGateway.navigateToHomePage();

        config.logStep("Click Buy Now on HomePage to open the order form");
        OrderFormPage orderForm = home.clickBuyNow();

        config.logStep("Fill dummy order details on the order form using the customer name, phone, and other dummy fields");
        orderForm.fillDummyOrderDetails(orderData);

        config.logStep("Get the displayed amount on the order form and record it");
        String orderFormAmount = orderForm.getDisplayedAmount();
        AssertHelper.assertEquals(config, orderFormAmount, "20,000", "Order form amount should show 20,000");

        config.logStep("Click Checkout on the order form to open the payment page");
        PaymentPopupPage paymentPopup = orderForm.clickCheckout();

        config.logStep("Open the order details overlay on the payment page");
        paymentPopup.openOrderDetailsOverlay();

        config.logStep("Verify the overlay customer name matches the customer name filled on the order form");
        AssertHelper.assertEquals(config, paymentPopup.getOverlayCustomerName(), orderData.getCustomerName(),
            "Overlay customer name should match the name filled on the order form");

        config.logStep("Verify the overlay customer phone matches the customer phone filled on the order form");
        boolean phonesMatch = paymentGateway.phoneNumbersMatch(paymentPopup.getOverlayCustomerPhone(), orderData.getCustomerPhone());
        AssertHelper.assertTrue(config, phonesMatch, "Overlay customer phone should match the phone filled on the order form (ignoring country code/trunk prefix)");

        config.logStep("Close the order details overlay");
        paymentPopup.closeOverlay();

        config.logStep("Verify the amount at the top of the payment page matches the amount recorded on the order form");
        String normalizedTopAmount = paymentGateway.normalizeAmount(paymentPopup.getTopAmountText());
        String normalizedOrderFormAmount = paymentGateway.normalizeAmount(orderFormAmount);
        AssertHelper.assertEquals(config, normalizedTopAmount, normalizedOrderFormAmount,
            "Top amount on payment page should match the amount recorded on the order form");

        config.logStep("Select Credit Card as the payment method");
        CreditCardPaymentPage creditCardPage = new CreditCardPaymentPage(config);
        creditCardPage.selectCreditCardMethod();

        config.logStep("Fill in the credit card details");
        creditCardPage.fillCardDetails(orderData);

        config.logStep("Get the amount shown before applying the promo and record it");
        String amountBeforePromo = creditCardPage.getAmountText();
        AssertHelper.assertEquals(config, amountBeforePromo, "Rp19.000", "Amount before promo should show Rp19.000");

        config.logStep("Apply the promo code on the credit card page");
        creditCardPage.applyPromo(orderData.getPromoCode());

        config.logStep("Get the amount after applying the promo and verify it decreased compared to the amount before applying it");
        String amountAfterPromo = creditCardPage.getAmountText();
        double beforePromoValue = Double.parseDouble(paymentGateway.normalizeAmount(amountBeforePromo));
        double afterPromoValue = Double.parseDouble(paymentGateway.normalizeAmount(amountAfterPromo));
        AssertHelper.assertTrue(config, afterPromoValue < beforePromoValue,
            "Amount after applying the promo should be less than the amount before the promo");

        config.logStep("Click Continue to proceed to the Issuing Bank page");
        IssuingBankPage issuingBank = creditCardPage.clickContinue();

        config.logStep("Verify the bank amount matches the amount after the promo was applied");
        String normalizedBankAmount = paymentGateway.normalizeAmount(issuingBank.getBankAmountText());
        String normalizedAmountAfterPromo = paymentGateway.normalizeAmount(amountAfterPromo);
        AssertHelper.assertEquals(config, normalizedBankAmount, normalizedAmountAfterPromo,
            "Bank amount should match the amount after the promo was applied");

        config.logStep("Enter the bank OTP on the Issuing Bank page");
        issuingBank.enterOtp(orderData.getBankOtp());

        config.logStep("Submit the OTP to complete the payment");
        PaymentSuccessPage successPage = issuingBank.submitOtp();

        config.logStep("Capture the amount and order id on the payment successful page immediately, since the page closes instantly");
        String successAmount = successPage.getSuccessAmountText();
        String successOrderId = successPage.getSuccessOrderId();

        config.logStep("Verify the amount on the payment successful page matches the amount after the promo was applied");
        AssertHelper.assertEquals(config, successAmount, amountAfterPromo,
            "Success amount should match the amount after the promo was applied");

        config.logStep("Verify the order id on the payment successful page is captured");
        AssertHelper.assertNotNull(config, successOrderId, "Order id should be captured on the success page");

        config.logStep("Wait for redirect back to the home page");
        WaitHelper.waitForNetworkIdle(config);

        config.logStep("Verify the thank you message is displayed on the home page with the expected text");
        AssertHelper.assertTrue(config, home.isThankYouMessageVisible(), "Thank you message should be visible after redirect to the home page");
        AssertHelper.assertEquals(config, home.getThankYouMessageText(), "Thank you for your purchase. Get a nice sleep.",
            "Thank you message text should match the expected message");
    }
}
