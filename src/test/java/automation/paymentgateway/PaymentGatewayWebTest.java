package automation.paymentgateway;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.PaymentGatewayHelper.OrderDetails;
import automation.modules.paymentgateway.PaymentGatewayHelper.PaymentReceipt;
import automation.modules.paymentgateway.PaymentGatewayEnums.PromoCode;

public class PaymentGatewayWebTest extends TestBase
{

    @Test(description = "Checkout an order, pay by credit card with a Flash Sale promo, complete bank OTP, and verify the final success details and thank-you redirect", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void payWithCreditCardApplyingPromoAndVerifyThankYouPage(Config config)
    {
        PaymentGatewayHelper paymentGateway = new PaymentGatewayHelper(config);
        PaymentGatewayData payment = paymentGateway.buildPayment();

        config.logStep("Open the shopping cart form and submit checkout details for the order");
        paymentGateway.checkout(payment);

        config.logStep("Open the order details overlay on the payment popup and verify the name and phone shown match what was entered in the checkout form");
        OrderDetails orderDetails = paymentGateway.viewOrderDetails();
        AssertHelper.assertEquals(config, orderDetails.getName(), payment.getName(), "Order details name should match the checkout form name");
        AssertHelper.assertEquals(config, orderDetails.getPhone(), payment.getPhone(), "Order details phone should match the checkout form phone");

        config.logStep("Close the order details overlay and verify the amount shown at the top of the payment page matches the amount entered during checkout");
        String amountBeforePromo = paymentGateway.closeOrderDetailsAndGetAmount();
        AssertHelper.assertEquals(config, amountBeforePromo, payment.getAmount(), "Displayed amount should match the checkout amount");

        config.logStep("Choose the Credit Card payment method, enter the card details, apply the Flash Sale promo, and verify the total amount decreases from the amount shown before the promo");
        String amountAfterPromo = paymentGateway.enterCreditCardDetails(payment, PromoCode.FlashSaleCreditCard);
        AssertHelper.assertTrue(config, Integer.parseInt(amountAfterPromo) < Integer.parseInt(amountBeforePromo), "Amount after promo should be less than the amount before promo");

        config.logStep("Continue to the Issuing Bank page and verify the amount shown matches the amount after the promo was applied");
        String bankAmount = paymentGateway.continueToIssuingBank();
        AssertHelper.assertEquals(config, bankAmount, amountAfterPromo, "Bank page amount should match the amount after promo");

        config.logStep("Enter the Bank OTP to finish the payment and immediately capture the payment successful page's amount and order id");
        PaymentReceipt receipt = paymentGateway.confirmBankOtp();
        AssertHelper.assertEquals(config, receipt.getAmount(), amountAfterPromo, "Receipt amount should match the amount after promo");
        AssertHelper.assertNotNull(config, receipt.getOrderId(), "Receipt order id should be present");

        config.logStep("Wait for the redirect back to the first page and verify the thank you message is displayed");
        String thankYouMessage = paymentGateway.verifyRedirectToThankYouPage();
        AssertHelper.assertEquals(config, thankYouMessage, "Thank you for your purchase. Get a nice sleep.", "Thank you message should be displayed after redirect");
    }
}
