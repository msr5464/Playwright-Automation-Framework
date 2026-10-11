package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.modules.paymentgateway.PaymentGatewayData;

public class CartFormPage extends BasePage
{
    private final Locator amountField;
    private final Locator nameField;
    private final Locator emailField;
    private final Locator addressField;
    private final Locator phoneField;
    private final Locator checkoutButton;

    public CartFormPage(Config config)
    {
        super(config);
        amountField = page.locator("input.text-right");
        nameField = page.locator("div.cart-inner tr:has-text('Name') input[type='text']");
        emailField = page.locator("input[type='email']");
        addressField = page.locator("div.cart-inner tr:has-text('Address') textarea");
        phoneField = page.locator("div.cart-inner tr:has-text('Phone no') input[type='text']");
        checkoutButton = page.locator("div.cart-checkout");
        assertPageLoaded(checkoutButton);
    }

    /**
     * Fill the amount, name, email, address and phone fields with the given payment
     * data, then click Checkout to proceed to the payment method page.
     */
    public PaymentMethodPage fillDetailsAndCheckout(PaymentGatewayData payment)
    {
        fillText(amountField, payment.getAmount(), "Amount field");
        fillText(nameField, payment.getCustomerName(), "Name field");
        fillText(emailField, payment.getEmail(), "Email field");
        fillText(addressField, payment.getAddress(), "Address field");
        fillText(phoneField, payment.getPhone(), "Phone field");
        click(checkoutButton, "Checkout button");
        return new PaymentMethodPage(config);
    }
}
