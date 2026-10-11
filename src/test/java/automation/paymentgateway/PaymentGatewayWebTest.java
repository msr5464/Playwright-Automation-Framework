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
import automation.modules.paymentgateway.PaymentGatewayEnums.PromoCode;
import automation.modules.paymentgateway.web.CreditCardFormPage;

import java.util.Map;

public class PaymentGatewayWebTest extends TestBase
{

    @Test(description = "Complete a Midtrans demo checkout by credit card with a promo code and verify details at each stage through to the success message", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void completePaymentViaCreditCardWithPromo(Config config)
    {
        PaymentGatewayHelper gateway = new PaymentGatewayHelper(config);
        PaymentGatewayData payment = buildPayment();

        config.logStep("Navigate to the Midtrans demo store, click Buy Now, fill the checkout form with the customer and payment details, and submit checkout");
        gateway.paymentMethodPage = gateway.startCheckout(payment);

        config.logStep("Open the order details overlay and verify the name and phone shown match the ones filled in the checkout form");
        gateway.paymentMethodPage.openDetailsOverlay();
        AssertHelper.assertEquals(config, gateway.paymentMethodPage.getOrderDetailsName(), payment.getCustomerName(), "Order details overlay should show the checkout form name");
        AssertHelper.assertEquals(config, gateway.paymentMethodPage.getOrderDetailsPhone(), payment.getPhone(), "Order details overlay should show the checkout form phone");

        config.logStep("Close the overlay and verify the amount shown at the top of the page matches the amount entered in the checkout form");
        gateway.paymentMethodPage.closeDetailsOverlay();
        String totalBeforePromo = gateway.paymentMethodPage.getTotalAmount();
        AssertHelper.assertEquals(config, totalBeforePromo, payment.getAmount(), "Total amount should match the amount entered in the checkout form");

        config.logStep("Choose Credit Card payment, enter the card details, apply the 'Promo Flash Sale (Credit-Card)' promo, and verify the total decreases after the promo is applied");
        gateway.creditCardFormPage = (CreditCardFormPage) gateway.paymentMethodPage.choosePaymentMethod(PaymentMethod.CreditCard);
        gateway.creditCardFormPage.fillCardDetails(payment);
        gateway.creditCardFormPage.applyPromo(PromoCode.FlashSaleCreditCard);
        String totalAfterPromo = gateway.creditCardFormPage.getTotalAmount();
        AssertHelper.assertTrue(config, Double.parseDouble(totalAfterPromo) < Double.parseDouble(totalBeforePromo), "Total should decrease after applying the promo");

        config.logStep("Continue to the Issuing Bank page and verify the amount shown matches the discounted total after the promo");
        gateway.issuingBankPage = gateway.creditCardFormPage.continueToBank();
        String bankAmount = gateway.issuingBankPage.getAmount();
        AssertHelper.assertEquals(config, bankAmount, totalAfterPromo, "Issuing bank amount should match the discounted total after the promo");

        config.logStep("Enter the Bank OTP to finish the payment and immediately verify the payment successful page shows the same amount as the Issuing Bank page and a captured order id");
        gateway.paymentSuccessPage = gateway.issuingBankPage.enterOtpAndFinish(payment.getBankOtp());
        AssertHelper.assertEquals(config, gateway.paymentSuccessPage.getAmount(), bankAmount, "Payment successful amount should match the Issuing Bank amount");
        AssertHelper.assertNotNull(config, gateway.paymentSuccessPage.getOrderId(), "Payment successful page should show a captured order id");

        config.logStep("Wait for the redirect back to the first page and verify it shows the thank-you message");
        gateway.storeHomePage = gateway.paymentSuccessPage.waitForRedirectToHome();
        AssertHelper.assertEquals(config, gateway.storeHomePage.getThankYouMessage(), "Thank you for your purchase.Get a nice sleep.", "Store home page should show the thank-you message after redirect");
    }

    /**
     * Load the credit-card-with-promo scenario row from paymentgateway-data.csv and build
     * the payment data from it. This is the test's one setup line.
     */
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
            .withExpiryDate(data.get("expiry_date"))
            .withCvv(data.get("cvv"))
            .withBankOtp(data.get("bank_otp"))
            .build();
    }
}
