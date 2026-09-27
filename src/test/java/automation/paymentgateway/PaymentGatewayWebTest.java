package automation.paymentgateway;

import automation.core.AssertHelper;
import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestDataReader;
import automation.core.TestVariables;
import automation.core.WaitHelper;
import automation.core.Enums.*;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.web.CheckoutFormPage;
import automation.modules.paymentgateway.web.CreditCardFormPage;
import automation.modules.paymentgateway.web.IssuingBankPage;
import automation.modules.paymentgateway.web.OrderDetailsOverlay;
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
        PaymentGatewayData data = helper.buildDefaultPaymentData();
        Map<String, String> testData = TestDataReader.loadCsvRowByColumnValue(
            "paymentgateway", "paymentgateway-data", "data_key", "checkout", Config.environment);

        config.logStep("Navigate to the Midtrans demo home page and verify the page loads with the Buy Now button");
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("paymentgateway.url"));
        PaymentGatewayHomePage home = new PaymentGatewayHomePage(config);

        config.logStep("Click Buy Now and verify the checkout form is displayed");
        CheckoutFormPage checkout = home.clickBuyNow();

        config.logStep("Fill the checkout form with customer name and phone, then click checkout to proceed to the payment page");
        PaymentPage paymentPage = checkout.fillAndCheckout(data.getName(), data.getPhone());

        config.logStep("Open the order details overlay by clicking the details icon");
        OrderDetailsOverlay overlay = paymentPage.clickDetails();

        config.logStep("Verify the customer name in the overlay matches the name entered in the checkout form");
        AssertHelper.assertEquals(config, overlay.getCustomerName(), data.getName(),
            "Customer name in order details overlay should match the name entered in the checkout form");

        config.logStep("Verify the customer phone in the overlay matches the phone entered in the checkout form");
        AssertHelper.assertEquals(config, overlay.getCustomerPhone(), data.getPhone(),
            "Customer phone in order details overlay should match the phone entered in the checkout form");

        config.logStep("Close the overlay and return to the payment page");
        PaymentPage paymentPageAfterOverlay = overlay.close();

        config.logStep("Verify the displayed amount on the payment page matches the expected purchase amount");
        String originalAmount = paymentPageAfterOverlay.getAmount();
        AssertHelper.assertEquals(config, originalAmount, testData.get("expected_amount"),
            "Amount displayed on the Payment page should match the expected purchase amount");

        config.logStep("Select Credit Card as the payment method and verify the credit card form is displayed");
        CreditCardFormPage cardPage = paymentPageAfterOverlay.selectCreditCard();

        config.logStep("Enter the card number in the credit card form");
        cardPage.enterCardNumber(data.getCardNumber());

        config.logStep("Enter the card expiry date in the credit card form");
        cardPage.enterExpiry(data.getExpiry());

        config.logStep("Enter the CVV in the credit card form");
        cardPage.enterCvv(data.getCvv());

        config.logStep("Select the Flash Sale promo and read the discounted amount shown after the promo is applied");
        cardPage.selectPromo(testData.get("promo_text"));
        String discountedAmount = cardPage.getAmountAfterPromo();

        config.logStep("Verify the discounted amount is less than the original amount, confirming the promo was applied");
        AssertHelper.assertTrue(config,
            helper.parseAmount(discountedAmount) < helper.parseAmount(originalAmount),
            "Amount after applying the Flash Sale promo should be less than the original amount");

        config.logStep("Click Continue to proceed to the issuing bank OTP page");
        IssuingBankPage bankPage = cardPage.clickContinue();

        config.logStep("Verify the amount on the issuing bank page matches the discounted amount");
        AssertHelper.assertEquals(config, bankPage.getAmount(), discountedAmount,
            "Amount on the issuing bank page should match the discounted amount from the credit card form");

        config.logStep("Enter the bank OTP to authenticate the payment");
        bankPage.enterOtp(data.getBankOtp());

        config.logStep("Submit the OTP and verify the payment success page is displayed");
        PaymentSuccessPage successPage = bankPage.submitOtp();

        config.logStep("Capture the payment amount and order ID from the success screen before the automatic redirect");
        PaymentGatewayData result = successPage.capturePaymentDetails();

        config.logStep("Verify the amount on the success screen is less than the original amount, confirming the promo discount was applied");
        AssertHelper.assertTrue(config,
            helper.parseAmount(result.getAmount()) < helper.parseAmount(originalAmount),
            "Amount after promo on success screen should be less than the original amount");

        config.logStep("Verify the order ID captured from the success screen is not null");
        AssertHelper.assertNotNull(config, result.getOrderId(),
            "Order ID captured from the payment success screen should not be null");

        config.logStep("Wait for the automatic post-payment redirect back to the home page");
        WaitHelper.waitForNetworkIdle(config);
        PaymentGatewayHomePage homePage = new PaymentGatewayHomePage(config);

        config.logStep("Verify the home page displays the thank-you message after the automatic post-payment redirect");
        AssertHelper.assertTrue(config, homePage.isThankYouMessageVisible(),
            "Thank-you notification should be visible on the home page after successful payment");
        AssertHelper.assertContains(config, homePage.getThankYouMessage(), testData.get("thank_you_message"),
            "Thank-you message text should match expected content");
    }
}
