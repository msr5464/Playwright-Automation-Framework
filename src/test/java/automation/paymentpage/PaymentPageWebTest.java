package automation.paymentpage;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.paymentpage.PaymentPageBuilder;
import automation.modules.paymentpage.PaymentPageData;
import automation.modules.paymentpage.PaymentPageHelper;
import automation.modules.paymentpage.web.CheckoutFormPage;
import automation.modules.paymentpage.web.HomePage;
import automation.modules.paymentpage.web.IssuingBankPage;
import automation.modules.paymentpage.web.PaymentPopupPage;
import automation.modules.paymentpage.web.PaymentSuccessPage;

public class PaymentPageWebTest extends TestBase
{
    @Test(description = "End-to-end credit card payment with promo discount on Midtrans demo \u2014 validates order details against filled data, discounted amount, issuing bank amount, success-page amount and order ID, and thank-you redirect",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void completeCreditCardPaymentWithPromo(Config config)
    {
        PaymentPageHelper helper = new PaymentPageHelper(config);
        PaymentPageData data = new PaymentPageBuilder().withDefaults().build();

        config.logStep("Navigate to the Midtrans demo home page");
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("paymentpage.url"));

        config.logStep("Click Buy Now button to open the checkout form");
        CheckoutFormPage checkoutForm = new HomePage(config).clickBuyNow();

        config.logStep("Fill firstName, lastName, email, and phone in the checkout form");
        checkoutForm.fillCheckoutDetails(data);

        config.logStep("Click Checkout button to open the payment popup");
        PaymentPopupPage paymentPopup = checkoutForm.clickCheckout();

        config.logStep("Click the Details icon to reveal order details");
        paymentPopup.clickDetailsIcon();

        config.logStep("Validate order customer name, email, and phone match the values filled in the checkout form");
        String expectedName = data.getFirstName() + " " + data.getLastName();
        AssertHelper.assertContains(config, paymentPopup.getOrderDetailsCustomerName(), expectedName, "Order customer name should match filled first and last name");
        AssertHelper.assertContains(config, paymentPopup.getOrderDetailsEmail(), data.getEmail(), "Order email should match filled email");
        AssertHelper.assertContains(config, paymentPopup.getOrderDetailsPhone(), data.getPhone(), "Order phone should match filled phone");

        config.logStep("Select Credit Card payment method");
        paymentPopup.selectCreditCard();

        config.logStep("Fill card number, expiry, and CVV from the generated test data");
        paymentPopup.fillCardNumber(data.getCardNumber());
        paymentPopup.fillExpiry(data.getExpiry());
        paymentPopup.fillCvv(data.getCvv());

        config.logStep("Capture the current amount displayed before applying the promo code");
        String originalAmount = paymentPopup.getAmountDisplay();

        config.logStep("Select the promo code from the dropdown");
        paymentPopup.selectPromo(data.getPromoCode());

        config.logStep("Validate the displayed amount has decreased after the promo code is applied");
        String discountedAmount = paymentPopup.getAmountDisplay();
        AssertHelper.assertTrue(config, !discountedAmount.equals(originalAmount), "Displayed amount should decrease after promo is applied");

        config.logStep("Click Continue button to proceed to the Issuing Bank page");
        IssuingBankPage issuingBank = paymentPopup.clickContinue();

        config.logStep("Validate the amount on the Issuing Bank page matches the discounted amount");
        AssertHelper.assertEquals(config, issuingBank.getAmountDisplay(), discountedAmount, "Issuing bank page amount should match the discounted amount");

        config.logStep("Enter OTP 112233 and click Submit to trigger the payment success page");
        PaymentSuccessPage successPage = issuingBank.enterOtp("112233").clickSubmit();

        config.logStep("Immediately capture amount and order ID from the payment success page before it auto-closes");
        PaymentPageData successDetails = helper.capturePaymentSuccessDetails(successPage);
        AssertHelper.assertNotNull(config, successDetails.getOrderId(), "Order ID should be present on the payment success page");
        AssertHelper.assertNotNull(config, successDetails.getOriginalAmount(), "Payment amount should be present on the payment success page");

        config.logStep("Wait for the automatic redirect back to the home page");
        HomePage homePage = new HomePage(config);

        config.logStep("Validate the thank-you message is visible on the home page after successful payment");
        AssertHelper.assertContains(config, homePage.getThankYouMessage(), "Thank you for your purchase", "Home page should display the thank-you message after payment");
    }
}
