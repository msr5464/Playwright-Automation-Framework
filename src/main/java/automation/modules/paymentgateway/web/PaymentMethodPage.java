package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.modules.paymentgateway.PaymentGatewayHelper;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;

public class PaymentMethodPage extends BasePage {
    private final Locator detailsIcon;
    private final Locator orderDetailsName;
    private final Locator orderDetailsPhone;
    private final Locator closeDetailsIcon;
    private final Locator totalAmountDisplay;

    public PaymentMethodPage(Config config) {
        super(config);
        detailsIcon = page.frameLocator("#snap-midtrans").locator("div.header-detail-clickable");
        orderDetailsName = page.frameLocator("#snap-midtrans")
                .locator("div.order-customer-group div:has-text('Mukesh Rajput')");
        orderDetailsPhone = page.frameLocator("#snap-midtrans").locator("div.order-summary-phone");
        closeDetailsIcon = page.frameLocator("#snap-midtrans")
                .locator("div.header-modal-content div.close-snap-button.clickable");
        totalAmountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-mukesh-amount");
        assertPageLoaded(totalAmountDisplay);
    }

    public void openDetailsOverlay() {
        click(detailsIcon, "Details icon");
    }

    public String getOrderDetailsName() {
        return getText(orderDetailsName, "Order details name");
    }

    public String getOrderDetailsPhone() {
        return getText(orderDetailsPhone, "Order details phone");
    }

    public void closeDetailsOverlay() {
        click(closeDetailsIcon, "Close details icon");
    }

    public String getTotalAmount() {
        return PaymentGatewayHelper.toPlainAmount(getText(totalAmountDisplay, "Total amount"));
    }

    /**
     * Select a payment method. The locator is the confirmed selector with only the
     * option's key replaced, so every option is selectable through the same code.
     * The option chosen decides the next page, so this returns BasePage and the
     * caller casts to the page the exercised value lands on.
     */
    public BasePage choosePaymentMethod(PaymentMethod method) {
        Locator option = page.frameLocator("#snap-midtrans").locator("a[href='" + method.getKey() + "']");
        click(option, method.getLabel() + " payment method");
        return switch (method) {
            case CreditCard -> new automation.modules.paymentgateway.web.CreditCardFormPage(config);
            default -> throw new UnsupportedOperationException(method.getLabel() + " is not automated yet");
        };
    }
}
