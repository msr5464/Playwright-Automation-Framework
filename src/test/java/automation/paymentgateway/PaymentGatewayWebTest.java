package automation.paymentgateway;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestDataReader;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.paymentgateway.PaymentGatewayBuilder;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.web.CreditCardPaymentPage;

import java.util.Map;

public class PaymentGatewayWebTest extends TestBase
{

    private PaymentGatewayData buildPayment()
    {
        Map<String, String> data = TestDataReader.loadCsvRowByColumnValue(
            "paymentgateway", "paymentgateway-data", "scenario", "credit_card_promo");
        return new PaymentGatewayBuilder()
            .withAmount(data.get("amount"))
            .withCustomerName(data.get("customer_name"))
            .withEmail(data.get("email"))
            .withAddress(data.get("address"))
            .withPhone(data.get("phone"))
            .withCardNumber(data.get("card_number"))
            .withCardExpiry(data.get("card_expiry"))
            .withCardCvv(data.get("card_cvv"))
            .withPromoCode(data.get("promo_code"))
            .withBankOtp(data.get("bank_otp"))
            .build();
    }

    @Test(description = "Checkout on the midtrans demo, pay by credit card with a promo, and verify the success and redirect details", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void payWithCreditCardAndPromoThenVerifySuccess(Config config)
    {
        PaymentGatewayHelper payments = new PaymentGatewayHelper(config);
        PaymentGatewayData payment = buildPayment();

        config.logStep("Navigate to the midtrans demo site, open the shopping cart form via Buy Now, fill in the customer and amount details, and submit checkout");
        payments.paymentMethodPage = payments.checkout(payment);

        config.logStep("Open the order details overlay and verify the name and phone match the details filled on the shopping cart form");
        payments.paymentMethodPage.openOrderDetails();
        AssertHelper.assertEquals(config, payments.paymentMethodPage.getOrderDetailsName(), payment.getCustomerName(), "Order details name should match the name filled earlier");
        AssertHelper.assertEquals(config, payments.paymentMethodPage.getOrderDetailsPhone(), payment.getPhone(), "Order details phone should match the phone filled earlier");

        config.logStep("Close the order details overlay and verify the amount shown at the top of the page matches the amount entered on the shopping cart form");
        payments.paymentMethodPage.closeOrderDetails();
        String amountBeforePromo = payments.paymentMethodPage.getTotalAmount();
        AssertHelper.assertEquals(config, amountBeforePromo, payment.getAmount(), "Total amount should match the amount entered on the shopping cart form");

        config.logStep("Choose Credit Card as the payment method and enter the card number, expiry and CVV");
        payments.creditCardPaymentPage = (CreditCardPaymentPage) payments.paymentMethodPage.choosePaymentMethod(PaymentMethod.CreditCard);
        payments.creditCardPaymentPage.fillCardDetails(payment);

        config.logStep("Apply the promo code and verify the total decreases from the amount recorded before the promo");
        payments.creditCardPaymentPage.applyPromo(payment.getPromoCode());
        String amountAfterPromo = payments.creditCardPaymentPage.getTotal();
        AssertHelper.assertTrue(config, Double.parseDouble(amountAfterPromo) < Double.parseDouble(amountBeforePromo), "Total after promo should be less than the total before the promo");

        config.logStep("Continue to the issuing bank page and verify the amount shown matches the discounted total after the promo");
        payments.issuingBankPage = payments.creditCardPaymentPage.continuePayment();
        AssertHelper.assertEquals(config, payments.issuingBankPage.getAmount(), amountAfterPromo, "Issuing bank amount should match the discounted total");

        config.logStep("Enter the bank OTP to finish the payment");
        payments.paymentSuccessPage = payments.issuingBankPage.enterOtp(payment.getBankOtp());

        config.logStep("Immediately capture the amount and order id shown on the payment successful page before it closes, and verify the amount matches the discounted total");
        String successAmount = payments.paymentSuccessPage.getSuccessAmount();
        String successOrderId = payments.paymentSuccessPage.getOrderId();
        AssertHelper.assertEquals(config, successAmount, amountAfterPromo, "Success page amount should match the discounted total");
        AssertHelper.assertNotNull(config, successOrderId, "Success page should display an order id");

        config.logStep("Wait for the redirect back to the original page and verify it shows the thank-you message");
        payments.landingPage = payments.paymentSuccessPage.waitForRedirectToLanding();
        AssertHelper.assertEquals(config, payments.landingPage.getThankYouMessage(), "Thank you for your purchase. Get a nice sleep.", "Landing page should show the thank-you message after redirect");
    }
}
