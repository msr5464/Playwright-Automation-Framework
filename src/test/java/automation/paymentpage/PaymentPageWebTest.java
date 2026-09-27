package automation.paymentpage;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestDataReader;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.paymentpage.PaymentPageBuilder;
import automation.modules.paymentpage.PaymentPageData;
import automation.modules.paymentpage.web.CheckoutFormPage;
import automation.modules.paymentpage.web.DemoHomePage;
import automation.modules.paymentpage.web.IssuingBankPage;
import automation.modules.paymentpage.web.PaymentPage;
import automation.modules.paymentpage.web.PaymentSuccessPage;

import java.util.Map;

public class PaymentPageWebTest extends TestBase
{
    @Test(description = "Fill checkout form, apply Promo Flash Sale on credit card, complete OTP, and validate success",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void completeCreditCardPaymentWithPromo(Config config)
    {
        Map<String, String> testData = TestDataReader.loadCsvRowByColumnValue(
            "paymentpage", "paymentpage-data", "data_key", "checkout", Config.environment);

        PaymentPageData data = new PaymentPageBuilder()
            .withName(testData.get("name"))
            .withPhone(testData.get("phone"))
            .build();

        config.logStep("Navigate to the Midtrans demo site and verify the home page loads");
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("paymentpage.url"));
        DemoHomePage homePage = new DemoHomePage(config);

        config.logStep("Click Buy Now and verify the checkout form page loads");
        CheckoutFormPage checkoutForm = homePage.clickBuyNow();

        config.logStep("Fill the checkout form with customer name and phone details");
        checkoutForm.fillForm(data);

        config.logStep("Click Checkout and verify the payment selection page loads");
        PaymentPage payment = checkoutForm.clickCheckout();

        config.logStep("Click Details to expand the order details panel");
        payment.clickDetails();

        config.logStep("Verify customer name in the payment panel matches the checkout form input");
        String orderName = payment.getOrderName();
        AssertHelper.assertEquals(config, orderName, data.getName(), "Order name in payment panel should match checkout form input");

        config.logStep("Verify customer phone in the payment panel matches the checkout form input");
        String orderPhone = payment.getOrderPhone();
        AssertHelper.assertEquals(config, orderPhone, data.getPhone(), "Order phone in payment panel should match checkout form input");

        config.logStep("Select Credit Card as the payment method");
        payment.selectCreditCard();

        config.logStep("Enter card number 4111111111111111 on the payment form");
        payment.enterCardNumber("4111111111111111");

        config.logStep("Enter expiry date 01/35 on the payment form");
        payment.enterExpiry("01/35");

        config.logStep("Enter CVV 123 on the payment form");
        payment.enterCvv("123");

        config.logStep("Capture the payable amount displayed before applying any promo");
        String amountBeforePromo = payment.getAmount();

        config.logStep("Select Promo Flash Sale (Credit-Card) to apply the discount");
        payment.selectPromo();

        config.logStep("Capture the payable amount after applying the Promo Flash Sale");
        String amountAfterPromo = payment.getAmount();

        config.logStep("Verify the Promo Flash Sale reduces the payable amount below the original amount");
        long amountBefore = Long.parseLong(amountBeforePromo.replaceAll("[^0-9]", ""));
        long amountAfter  = Long.parseLong(amountAfterPromo.replaceAll("[^0-9]", ""));
        AssertHelper.assertTrue(config, amountAfter < amountBefore, "Amount after applying Promo Flash Sale should be less than the original amount");

        config.logStep("Click Continue and verify the Issuing Bank page loads");
        IssuingBankPage issuingBank = payment.clickContinue();

        config.logStep("Verify the Issuing Bank page displays the post-promo discounted amount");
        String issuingBankAmount = issuingBank.getAmount();
        AssertHelper.assertEquals(config, issuingBankAmount, amountAfterPromo, "Issuing Bank page amount should match the discounted amount after applying promo");

        config.logStep("Enter OTP 112233 and submit to proceed to the Payment Successful page");
        issuingBank.enterOtp("112233");
        PaymentSuccessPage success = issuingBank.submit();

        config.logStep("Immediately capture the success amount and order ID from the Payment Successful page");
        String successAmount  = success.getAmount();
        String successOrderId = success.getOrderId();

        config.logStep("Verify the Payment Successful page shows the correct discounted amount");
        AssertHelper.assertEquals(config, successAmount, amountAfterPromo, "Payment success amount should match the post-promo discounted amount");

        config.logStep("Verify the Payment Successful page displays a valid order ID");
        AssertHelper.assertNotNull(config, successOrderId, "Payment Successful page should display a valid order ID");

        config.logStep("Wait for the Payment Successful page to close automatically and redirect back to the home page");
        DemoHomePage returnedHome = new DemoHomePage(config);

        config.logStep("Verify the home page displays the Thank You confirmation message after a successful purchase");
        AssertHelper.assertTrue(config, returnedHome.isThankYouMessageVisible(), "Home page should display the Thank You message after a successful payment");
    }
}
