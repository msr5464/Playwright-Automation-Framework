package automation.paymentgateway;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestDataReader;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.paymentgateway.PaymentGatewayBuilder;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.web.CreditCardFormPage;
import automation.modules.paymentgateway.web.IssuingBankPage;
import automation.modules.paymentgateway.web.PaymentGatewayHomePage;
import automation.modules.paymentgateway.web.PaymentPage;
import automation.modules.paymentgateway.web.PaymentSuccessPage;
import org.testng.annotations.Test;

import java.util.Map;

public class PaymentGatewayWebTest extends TestBase
{
    @Test(description = "Complete a full credit card payment via Midtrans demo and verify all checkpoints end-to-end",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void completeCreditCardPayment(Config config)
    {
        PaymentGatewayHelper helper = new PaymentGatewayHelper(config);
        Map<String, String> testData = TestDataReader.loadCsvRowByColumnValue(
            "paymentgateway", "paymentgateway-data", "test_key", "complete_payment", Config.environment);

        PaymentGatewayData data    = new PaymentGatewayBuilder().build();
        String expectedAmount       = testData.get("expected_amount");
        String promoName            = testData.get("promo_name");
        String expectedThankYou     = testData.get("thank_you_message");

        config.logStep("Navigate to the Midtrans demo home page, click Buy Now, and fill the checkout form to proceed to the Payment page");
        PaymentPage paymentPage = helper.fillCheckoutAndProceed(data);

        config.logStep("Open the order details overlay, read customer name and phone, and close the overlay");
        PaymentGatewayData orderDetails = helper.getOrderDetails(paymentPage);

        config.logStep("Verify the customer name in the overlay matches the name entered in the checkout form");
        AssertHelper.assertEquals(config, orderDetails.getName(), data.getName(),
            "Customer name in order details overlay should match the checkout form input");

        config.logStep("Verify the customer phone in the overlay matches the phone entered in the checkout form");
        AssertHelper.assertEquals(config,
            helper.normalizePhone(orderDetails.getPhone()),
            helper.normalizePhone(data.getPhone()),
            "Customer phone in order details overlay should match the checkout form input (normalised)");

        config.logStep("Record the original payment amount displayed on the Payment page");
        String originalAmount = paymentPage.getAmount();

        config.logStep("Verify the displayed amount matches the expected purchase amount");
        AssertHelper.assertEquals(config, originalAmount, expectedAmount,
            "Payment page amount should match the expected purchase amount");

        config.logStep("Select Credit Card, enter card number, expiry, and CVV, then apply the flash sale promo");
        CreditCardFormPage cardForm = helper.selectAndFillCreditCard(paymentPage, data, promoName);

        config.logStep("Capture the displayed amount after the flash sale promo has been applied");
        String discountedAmount = cardForm.getAmountAfterPromo();

        config.logStep("Verify the discounted amount is less than the original pre-promo amount");
        AssertHelper.assertTrue(config,
            helper.parseAmount(discountedAmount) < helper.parseAmount(originalAmount),
            "Amount after promo should be less than the original amount");

        config.logStep("Click Continue to proceed from the Credit Card form to the Issuing Bank 3DS verification page");
        IssuingBankPage bankPage = cardForm.clickContinue();

        config.logStep("Verify the Issuing Bank page amount matches the discounted amount");
        AssertHelper.assertEquals(config,
            helper.parseAmount(bankPage.getAmount()),
            helper.parseAmount(discountedAmount),
            "Issuing bank page amount should equal the discounted amount after promo");

        config.logStep("Enter the bank OTP on the Issuing Bank 3DS verification page");
        bankPage.enterOtp(data.getBankOtp());

        config.logStep("Submit the OTP to complete the payment and arrive at the Payment Success page");
        PaymentSuccessPage successPage = bankPage.submitOtp();

        config.logStep("Capture the payment amount and order ID from the success page before the auto-redirect");
        PaymentGatewayData captured = successPage.capturePaymentDetails();

        config.logStep("Verify the success page amount matches the discounted amount applied during checkout");
        AssertHelper.assertEquals(config, captured.getAmount(), discountedAmount,
            "Success page amount should match the discounted amount");

        config.logStep("Verify the success page displays a non-null order ID");
        AssertHelper.assertNotNull(config, captured.getOrderId(),
            "Success page should display a non-null order ID");

        config.logStep("Wait for the automatic post-payment redirect back to the home page");
        PaymentGatewayHomePage homePage = helper.waitForSuccessRedirect();

        config.logStep("Verify the thank-you message on the home page matches the expected text after successful payment");
        AssertHelper.assertEquals(config, homePage.getThankYouMessage(), expectedThankYou,
            "Home page should display the expected thank-you message after successful payment");
    }
}
