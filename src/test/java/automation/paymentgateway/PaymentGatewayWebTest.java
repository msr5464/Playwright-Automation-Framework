package automation.paymentgateway;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.WaitHelper;
import automation.core.Enums.*;
import automation.modules.paymentgateway.PaymentGatewayBuilder;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.web.CreditCardPaymentPage;
import automation.modules.paymentgateway.web.IssuingBankPage;
import automation.modules.paymentgateway.web.MidtransLandingPage;
import automation.modules.paymentgateway.web.PaymentOverlayPage;
import automation.modules.paymentgateway.web.PaymentSuccessPage;
import automation.modules.paymentgateway.web.ShoppingCartFormPage;

public class PaymentGatewayWebTest extends TestBase
{

    @Test(description = "Complete a Midtrans demo purchase with Credit Card + promo, validating order details, amounts at each stage, and the final thank-you redirect",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void purchaseViaCreditCardWithPromoAndValidateDetails(Config config)
    {
        PaymentGatewayHelper paymentGateway = new PaymentGatewayHelper(config);
        PaymentGatewayData paymentData = new PaymentGatewayBuilder().build();

        config.logStep("Navigate to the Midtrans demo checkout landing page");
        MidtransLandingPage landingPage = paymentGateway.navigateToLandingPage();

        config.logStep("Click Buy Now to open the shopping cart form");
        ShoppingCartFormPage cartForm = landingPage.clickBuyNow();

        config.logStep("Fill the shopping cart form with amount, name, email, phone and address, then submit checkout");
        PaymentOverlayPage overlay = cartForm.fillAndSubmitCheckoutForm(paymentData);

        config.logStep("Open the order details overlay");
        overlay.clickDetailsIcon();

        config.logStep("Verify the order details name matches the name entered on the checkout form");
        AssertHelper.assertEquals(config, overlay.getOrderDetailsName(), paymentData.getName(), "Order details name should match the name entered on the checkout form");

        config.logStep("Verify the order details phone matches the phone entered on the checkout form");
        AssertHelper.assertEquals(config, overlay.getOrderDetailsPhone(), paymentData.getPhone(), "Order details phone should match the phone entered on the checkout form");

        config.logStep("Close the overlay");
        overlay.closeOverlay();

        config.logStep("Verify the amount at the top of the page matches the amount entered on the checkout form");
        String amountBeforePromo = paymentGateway.extractPlainAmount(overlay.getAmountDisplayed());
        String enteredAmount = paymentGateway.extractPlainAmount(paymentData.getAmount());
        AssertHelper.assertEquals(config, amountBeforePromo, enteredAmount, "Amount displayed should match the amount entered on the checkout form");

        config.logStep("Select the Credit Card payment method");
        CreditCardPaymentPage creditCardPage = overlay.selectCreditCardMethod();

        config.logStep("Enter card number, expiry and CVV");
        creditCardPage.fillCardNumber(paymentData.getCardNumber());
        creditCardPage.fillExpiry(paymentData.getCardExpiry());
        creditCardPage.fillCvv(paymentData.getCardCvv());

        config.logStep("Select the promo");
        creditCardPage.selectPromo(paymentData.getPromoName());

        config.logStep("Verify the discounted amount is lower than the amount validated earlier");
        String rawAmountAfterPromo = creditCardPage.getAmountDisplayed();
        String amountAfterPromo = paymentGateway.extractPlainAmount(rawAmountAfterPromo);
        AssertHelper.assertTrue(config, Integer.parseInt(amountAfterPromo) < Integer.parseInt(amountBeforePromo), "Discounted amount should be lower than the amount before applying the promo");

        config.logStep("Click Continue to proceed to the issuing bank page");
        IssuingBankPage issuingBankPage = creditCardPage.clickContinue();

        config.logStep("Verify the amount on the issuing bank page matches the discounted amount");
        AssertHelper.assertEquals(config, paymentGateway.extractPlainAmount(issuingBankPage.getAmountDisplayed()), amountAfterPromo, "Issuing bank amount should match the discounted amount shown after applying the promo");

        config.logStep("Enter the bank OTP to complete the payment and record the amount and order id shown on the success page immediately, since the page closes instantly");
        issuingBankPage.fillOtp(paymentData.getOtp());
        PaymentSuccessPage successPage = issuingBankPage.submit();
        String successAmount = successPage.getAmountDisplayed();
        String successOrderId = successPage.getOrderId();
        AssertHelper.assertNotNull(config, successOrderId, "Order id should be present on the payment successful page");

        config.logStep("Verify the amount on the payment successful page matches the discounted amount shown after applying the promo");
        AssertHelper.assertEquals(config, successAmount, rawAmountAfterPromo, "Payment successful amount should match the discounted amount shown after applying the promo");

        config.logStep("Wait for the redirect back to the original page");
        WaitHelper.waitForNetworkIdle(config);

        config.logStep("Verify the thank-you message is displayed");
        AssertHelper.assertEquals(config, landingPage.getThankYouMessageText().replaceAll("\\s+", "").toLowerCase(), "Thank you for your purchase. Get a nice sleep.".replaceAll("\\s+", "").toLowerCase(), "Thank you message should be displayed after successful payment");
    }
}
