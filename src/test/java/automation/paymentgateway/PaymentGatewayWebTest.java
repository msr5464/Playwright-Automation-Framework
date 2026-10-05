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
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.PaymentGatewayHelper.OrderDetails;
import automation.modules.paymentgateway.PaymentGatewayHelper.PaymentSuccessDetails;
import automation.modules.paymentgateway.PaymentGatewayHelper.PromoResult;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;
import automation.modules.paymentgateway.PaymentGatewayEnums.Promo;

import java.util.Map;

public class PaymentGatewayWebTest extends TestBase
{

    @Test(description = "Checkout on the Midtrans demo store, pay by credit card with a promo applied, and verify amount/order id through to the final thank-you redirect", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void payByCreditCardAndVerifySuccessfulPayment(Config config)
    {
        PaymentGatewayHelper paymentGateway = new PaymentGatewayHelper(config);
        Map<String, String> testData = TestDataReader.loadCsvRowByColumnValue(
            "paymentgateway", "paymentgateway-data", "data_key", "credit_card_payment");
        PaymentGatewayData payment = new PaymentGatewayBuilder()
            .withAmount(testData.get("amount"))
            .withCardNumber(testData.get("card_number"))
            .withExpiry(testData.get("expiry"))
            .withCvv(testData.get("cvv"))
            .withOtp(testData.get("otp"))
            .build();

        config.logStep("Open the shopping cart, fill in the checkout details and submit, then open the order details overlay and verify the name and phone match what was filled");
        OrderDetails orderDetails = paymentGateway.startCheckoutAndOpenOrderDetails(payment);
        AssertHelper.assertEquals(config, orderDetails.getName(), payment.getName(), "Order details name should match the name filled in the cart form");
        AssertHelper.assertEquals(config, orderDetails.getPhone(), payment.getPhone(), "Order details phone should match the phone filled in the cart form");

        config.logStep("Close the overlay and verify the amount shown at the top of the page matches the amount entered on the cart form");
        String amountOnSnapPage = paymentGateway.closeOrderDetailsAndGetAmount();
        AssertHelper.assertEquals(config, amountOnSnapPage, payment.getAmount(), "Amount on Snap payment page should match the amount entered on the cart form");

        config.logStep("Choose Credit Card as the payment method, enter the card details and apply the Flash Sale promo, and verify the amount decreases after the promo is applied");
        PromoResult promoResult = paymentGateway.choosePaymentMethodAndApplyPromo(PaymentMethod.CreditCard, Promo.FlashSaleCreditCard, payment);
        AssertHelper.assertTrue(config, Double.parseDouble(promoResult.getAmountAfter()) < Double.parseDouble(promoResult.getAmountBefore()), "Amount should decrease after the promo is applied");

        config.logStep("Continue to the Issuing Bank page and verify the amount matches the amount after the promo was applied");
        String amountOnBankPage = paymentGateway.continueToIssuingBank();
        AssertHelper.assertEquals(config, amountOnBankPage, promoResult.getAmountAfter(), "Amount on Issuing Bank page should match the amount after the promo was applied");

        config.logStep("Enter the bank OTP to finish the payment and immediately capture the payment successful page's amount and order id, and verify the amount matches the bank page and an order id is present");
        PaymentSuccessDetails successDetails = paymentGateway.enterBankOtpAndCompletePayment(payment);
        AssertHelper.assertEquals(config, successDetails.getAmount(), amountOnBankPage, "Payment successful amount should match the amount on the Issuing Bank page");
        AssertHelper.assertNotNull(config, successDetails.getOrderId(), "Payment successful page should display an order id");

        config.logStep("Wait for the redirect back to the first page and verify the thank-you message is shown");
        String message = paymentGateway.waitForRedirectHomeAndGetMessage();
        AssertHelper.assertEquals(config, message, "Thank you for your purchase.Get a nice sleep.", "Home page should show the thank-you message after redirect");
    }
}
