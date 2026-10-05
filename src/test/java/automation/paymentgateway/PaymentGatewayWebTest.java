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
        PaymentGatewayData payment = buildPaymentData();

        config.logStep("Open the shopping cart, fill in the checkout details and submit, then open the order details overlay and verify the name and phone match what was filled");
        PaymentGatewayHelper.OrderDetails orderDetails = paymentGateway.startCheckoutAndOpenOrderDetails(payment);
        AssertHelper.assertEquals(config, orderDetails.getName(), payment.getName(), "Order details name should match the cart form name");
        AssertHelper.assertEquals(config, orderDetails.getPhone(), payment.getPhone(), "Order details phone should match the cart form phone");

        config.logStep("Close the overlay and verify the amount shown at the top of the page matches the amount entered on the cart form");
        String amountOnSnapPage = paymentGateway.closeOrderDetailsAndGetAmount();
        AssertHelper.assertEquals(config, amountOnSnapPage, payment.getAmount(), "Amount on payment popup should match the cart form amount");

        config.logStep("Choose Credit Card as the payment method, enter the card details and apply the Flash Sale promo, and verify the amount decreases after the promo is applied");
        PaymentGatewayHelper.PromoResult promoResult = paymentGateway.choosePaymentMethodAndApplyPromo(PaymentMethod.CreditCard, Promo.FlashSaleCreditCard, payment);
        AssertHelper.assertTrue(config, Integer.parseInt(promoResult.getAmountAfter()) < Integer.parseInt(promoResult.getAmountBefore()), "Amount after promo should be less than amount before promo");

        config.logStep("Continue to the Issuing Bank page and verify the amount matches the amount after the promo was applied");
        String amountOnBankPage = paymentGateway.continueToIssuingBank();
        AssertHelper.assertEquals(config, amountOnBankPage, promoResult.getAmountAfter(), "Bank page amount should match the amount after the promo was applied");

        config.logStep("Enter the bank OTP to finish the payment and immediately capture the payment successful page's amount and order id, and verify the amount matches the bank page and an order id is present");
        PaymentGatewayHelper.PaymentSuccessDetails successDetails = paymentGateway.enterBankOtpAndCompletePayment(payment);
        AssertHelper.assertEquals(config, successDetails.getAmount(), amountOnBankPage, "Success page amount should match the bank page amount");
        AssertHelper.assertNotNull(config, successDetails.getOrderId(), "Order id should be present on the success page");

        config.logStep("Wait for the redirect back to the first page and verify the thank-you message is shown");
        String message = paymentGateway.waitForRedirectHomeAndGetMessage();
        AssertHelper.assertEquals(config, message, "Thank you for your purchase. Get a nice sleep.", "Thank you message should be shown after redirect");
    }

    /**
     * Loads the checkout scenario from paymentgateway-data.csv and builds the
     * PaymentGatewayData for the test, keeping the test-case-given values
     * (amount, name, email, address, card number, expiry, cvv, otp) exact.
     */
    private PaymentGatewayData buildPaymentData()
    {
        Map<String, String> data = TestDataReader.loadCsvRowByColumnValue(
            "paymentgateway", "paymentgateway-data", "checkout_key", "checkout", Config.environment);
        return new PaymentGatewayBuilder()
            .withAmount(data.get("amount"))
            .withName(data.get("name"))
            .withEmail(data.get("email"))
            .withAddress(data.get("address"))
            .withCardNumber(data.get("card_number"))
            .withExpiry(data.get("expiry"))
            .withCvv(data.get("cvv"))
            .withOtp(data.get("otp"))
            .build();
    }
}
