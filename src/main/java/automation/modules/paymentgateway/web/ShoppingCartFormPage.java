package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.modules.paymentgateway.PaymentGatewayData;

public class ShoppingCartFormPage extends BasePage
{
    private final Locator amountField;
    private final Locator nameField;
    private final Locator emailField;
    private final Locator phoneField;
    private final Locator addressField;
    private final Locator checkoutButton;

    public ShoppingCartFormPage(Config config)
    {
        super(config);
        amountField = page.locator("input.text-right");
        nameField = page.locator("div.cart-inner tr:has-text('Name') input[type='text']");
        emailField = page.locator("input[type='email']");
        phoneField = page.locator("div.cart-inner tr:has-text('Phone no') input[type='text']");
        addressField = page.locator("div.cart-inner tr:has-text('Address') textarea");
        checkoutButton = page.locator("div.cart-checkout");
        assertPageLoaded(checkoutButton);
    }

    public ShoppingCartFormPage fillCustomerDetails(PaymentGatewayData payment)
    {
        Log.comment(config, "Filling customer and amount details on the shopping cart form");
        fillText(amountField, payment.getAmount(), "Amount field");
        fillText(nameField, payment.getCustomerName(), "Name field");
        fillText(emailField, payment.getEmail(), "Email field");
        fillText(phoneField, payment.getPhone(), "Phone field");
        fillText(addressField, payment.getAddress(), "Address field");
        return this;
    }

    public PaymentMethodPage checkout()
    {
        Log.comment(config, "Clicking Checkout to proceed to payment method selection");
        click(checkoutButton, "Checkout button");
        return new PaymentMethodPage(config);
    }
}
