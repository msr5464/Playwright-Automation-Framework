package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class PaymentGatewayCheckoutFormPage extends BasePage
{
    private final Locator amountField;
    private final Locator nameField;
    private final Locator emailField;
    private final Locator phoneField;
    private final Locator addressField;
    private final Locator checkoutButton;

    public PaymentGatewayCheckoutFormPage(Config config)
    {
        super(config);
        amountField = page.locator("div.cart-inner input.text-right");
        nameField = page.locator("div.cart-inner tr:has(td:has-text('Name')) input[type='text']");
        emailField = page.locator("div.cart-inner input[type='email']");
        phoneField = page.locator("div.cart-inner tr:has(td:has-text('Phone')) input[type='text']");
        addressField = page.locator("div.cart-inner tr:has(td:has-text('Address')) textarea");
        checkoutButton = page.locator("div.cart-checkout");
        assertPageLoaded(amountField);
    }

    public void fillAmount(String amount)
    {
        fillText(amountField, amount, "Amount field");
    }

    public void fillName(String name)
    {
        fillText(nameField, name, "Name field");
    }

    public void fillEmail(String email)
    {
        fillText(emailField, email, "Email field");
    }

    public void fillPhone(String phone)
    {
        fillText(phoneField, phone, "Phone field");
    }

    public void fillAddress(String address)
    {
        fillText(addressField, address, "Address field");
    }

    public PaymentGatewayPaymentPopupPage clickCheckout()
    {
        Log.comment(config, "Clicking Checkout button on the shopping cart form");
        click(checkoutButton, "Checkout button");
        return new PaymentGatewayPaymentPopupPage(config);
    }
}
