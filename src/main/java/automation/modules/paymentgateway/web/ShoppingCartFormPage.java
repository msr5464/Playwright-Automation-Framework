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

    public PaymentOverlayPage clickCheckout()
    {
        Log.comment(config, "Clicking Checkout button on shopping cart form");
        click(checkoutButton, "Checkout button");
        return new PaymentOverlayPage(config);
    }

    public PaymentOverlayPage fillAndSubmitCheckoutForm(PaymentGatewayData paymentData)
    {
        Log.comment(config, "Filling checkout form with amount, name, email, phone and address");
        fillAmount(paymentData.getAmount());
        fillName(paymentData.getName());
        fillEmail(paymentData.getEmail());
        fillPhone(paymentData.getPhone());
        fillAddress(paymentData.getAddress());
        return clickCheckout();
    }
}
