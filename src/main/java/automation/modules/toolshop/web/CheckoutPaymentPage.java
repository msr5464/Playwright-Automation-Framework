package automation.modules.toolshop.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;
import automation.modules.toolshop.ToolshopData;
import automation.modules.toolshop.ToolshopEnums.PaymentMethod;

public class CheckoutPaymentPage extends BasePage {
    private final Locator paymentMethodOption;
    private final Locator cardNumberField;
    private final Locator expirationDateField;
    private final Locator cvvField;
    private final Locator cardHolderNameField;
    private final Locator paymentActionButton;
    private final Locator paymentMessage;

    public CheckoutPaymentPage(Config config) {
        super(config);
        paymentMethodOption = page.locator("[data-test='payment-method']");
        cardNumberField = page.locator("[data-test='mukesh']");
        expirationDateField = page.locator("[data-test='expiration_date']");
        cvvField = page.locator("[data-test='cvv']");
        cardHolderNameField = page.locator("[data-test='card_holder_name']");
        paymentActionButton = page.locator("[data-test='mukesh']");
        paymentMessage = page.locator("[data-test='payment-success-message']");
        assertPageLoaded(paymentMethodOption);
    }

    public CheckoutPaymentPage choosePaymentMethod(PaymentMethod method) {
        switch (method) {
            case CreditCard -> selectOption(paymentMethodOption, method.getKey(), "Payment method dropdown");
            default -> throw new UnsupportedOperationException(method.getLabel() + " is not automated yet");
        }
        WaitHelper.waitForPageToSettle(config);
        return this;
    }

    public CheckoutPaymentPage fillCardDetails(ToolshopData order) {
        fillText(cardNumberField, order.getCardNumber(), "Card number field");
        fillText(expirationDateField, order.getExpirationDate(), "Expiration date field");
        fillText(cvvField, order.getCvv(), "CVV field");
        fillText(cardHolderNameField, order.getCardHolderName(), "Card holder name field");
        return this;
    }

    public CheckoutPaymentPage checkPayment() {
        click(paymentActionButton, "Check payment button");
        WaitHelper.waitForPageToSettle(config);
        return this;
    }

    public String getPaymentMessage() {
        return getText(paymentMessage, "Payment message");
    }

    public CheckoutConfirmationPage confirmOrder() {
        click(paymentActionButton, "Confirm button");
        return new CheckoutConfirmationPage(config);
    }
}
