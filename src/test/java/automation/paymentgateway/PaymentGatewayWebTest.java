package automation.paymentgateway;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.web.PaymentGatewayCheckoutFormPage;
import automation.modules.paymentgateway.web.PaymentGatewayCreditCardPage;
import automation.modules.paymentgateway.web.PaymentGatewayIssuingBankPage;
import automation.modules.paymentgateway.web.PaymentGatewayLandingPage;
import automation.modules.paymentgateway.web.PaymentGatewayPaymentPopupPage;
import automation.modules.paymentgateway.web.PaymentGatewaySuccessPage;

public class PaymentGatewayWebTest extends TestBase
{

    @Test(description = "Complete a Midtrans checkout paying by credit card, applying a promo, and verify order details, amounts and the final redirect message across the flow", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void completePaymentViaCreditCardWithPromo(Config config)
    {
        PaymentGatewayHelper paymentGateway = new PaymentGatewayHelper(config);
        PaymentGatewayData data = paymentGateway.buildCheckoutData();

        config.logStep("Navigate to the Midtrans demo landing page");
        PaymentGatewayLandingPage landing = paymentGateway.navigateToLandingPage();

        config.logStep("Click Buy Now to open the shopping cart form");
        PaymentGatewayCheckoutFormPage checkoutForm = landing.clickBuyNow();

        config.logStep("Fill Amount, Name, Email, Address and other required fields with dummy data, recording the entered name and phone");
        checkoutForm.fillAmount(data.getAmount());
        checkoutForm.fillName(data.getName());
        checkoutForm.fillEmail(data.getEmail());
        checkoutForm.fillAddress(data.getAddress());
        checkoutForm.fillPhone(data.getPhone());
        String enteredName = data.getName();
        String enteredPhone = data.getPhone();

        config.logStep("Click Checkout to open the payment popup");
        PaymentGatewayPaymentPopupPage paymentPopup = checkoutForm.clickCheckout();

        config.logStep("Click the Details icon to open the order details overlay");
        paymentPopup.openDetailsOverlay();

        config.logStep("Verify the order details overlay name matches the name entered on the checkout form");
        String orderDetailsName = paymentPopup.getOrderDetailsName();
        AssertHelper.assertEquals(config, orderDetailsName, enteredName, "Order details overlay name should match the name entered on the checkout form");

        config.logStep("Verify the order details overlay phone matches the phone entered on the checkout form");
        String orderDetailsPhone = paymentPopup.getOrderDetailsPhone();
        String normalizedOverlayPhone = PaymentGatewayPaymentPopupPage.normalizePhoneDigits(orderDetailsPhone);
        String normalizedEnteredPhone = PaymentGatewayPaymentPopupPage.normalizePhoneDigits(enteredPhone);
        AssertHelper.assertEquals(config, normalizedOverlayPhone, normalizedEnteredPhone, "Order details overlay phone should match the phone entered on the checkout form");

        config.logStep("Close the overlay to return to the payment popup");
        paymentPopup.closeOverlay();

        config.logStep("Read and record the amount displayed at the top of the payment popup");
        String pageAmount = paymentPopup.getDisplayedAmount();

        config.logStep("Verify the amount displayed at the top of the payment popup matches the amount entered on the checkout form");
        AssertHelper.assertEquals(config, pageAmount, data.getAmount(), "Amount at the top of the payment popup should match the amount entered on the checkout form");

        config.logStep("Select Credit Card as the payment method");
        PaymentGatewayCreditCardPage creditCardPage = paymentPopup.selectCreditCardMethod();

        config.logStep("Enter card number, expiry and CVV");
        creditCardPage.fillCardNumber(data.getCardNumber());
        creditCardPage.fillExpiry(data.getCardExpiry());
        creditCardPage.fillCvv(data.getCardCvv());

        config.logStep("Apply the promo 'Flash Sale (Credit-Card)'");
        creditCardPage.selectPromo(data.getPromoCode());

        config.logStep("Read and record the amount displayed after applying the promo");
        String postPromoAmount = creditCardPage.getDisplayedAmount();

        config.logStep("Verify the amount after applying the promo is less than the amount recorded before the promo");
        AssertHelper.assertTrue(config, Integer.parseInt(postPromoAmount) < Integer.parseInt(pageAmount), "Amount after applying the promo should be less than the amount before the promo");

        config.logStep("Click Continue to reach the Issuing Bank page");
        PaymentGatewayIssuingBankPage issuingBankPage = creditCardPage.clickContinue();

        config.logStep("Read and record the amount displayed on the Issuing Bank page");
        String bankPageAmount = issuingBankPage.getDisplayedAmount();

        config.logStep("Verify the amount on the Issuing Bank page matches the amount recorded after applying the promo");
        AssertHelper.assertEquals(config, bankPageAmount, postPromoAmount, "Amount on the Issuing Bank page should match the amount recorded after applying the promo");

        config.logStep("Enter Bank OTP and submit");
        PaymentGatewaySuccessPage successPage = paymentGateway.submitOtpAndCompletePayment(issuingBankPage, data.getBankOtp());

        config.logStep("Immediately read and record the amount and order id displayed on the payment successful page");
        String successAmount = successPage.getDisplayedAmount();
        String successOrderId = successPage.getOrderId();

        config.logStep("Verify the amount on the payment successful page matches the amount recorded after applying the promo");
        AssertHelper.assertEquals(config, successAmount, postPromoAmount, "Amount on the payment successful page should match the amount recorded after applying the promo");

        config.logStep("Verify an order id is displayed on the payment successful page");
        AssertHelper.assertNotNull(config, successOrderId, "Order id should be displayed on the payment successful page");

        config.logStep("Wait for the automatic redirect back to the landing page");
        PaymentGatewayLandingPage redirectedLanding = paymentGateway.waitForRedirectToLandingPage();

        config.logStep("Verify the thank you message is displayed on the landing page");
        AssertHelper.assertEquals(config, PaymentGatewayLandingPage.normalizeMessage(redirectedLanding.getThankYouMessageText()), PaymentGatewayLandingPage.normalizeMessage("Thank you for your purchase. Get a nice sleep."), "Landing page should show the post-purchase thank you message");
    }
}
