package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class CheckoutFormPage extends BasePage
{
    private final Locator nameField;
    private final Locator phoneField;
    private final Locator checkoutButton;

    public CheckoutFormPage(Config config)
    {
        super(config);
        nameField      = page.locator(".cart-section:has(input[type='email']) tr:nth-child(1) input");
        phoneField     = page.locator(".cart-section:has(input[type='email']) tr:nth-child(3) input");
        checkoutButton = page.locator(".cart-checkout");
        assertPageLoaded(nameField);
    }

    public CheckoutFormPage fillName(String name)
    {
        Log.comment(config, "Filling customer name: " + name);
        fillText(nameField, name, "Name field");
        return this;
    }

    public CheckoutFormPage fillPhone(String phone)
    {
        Log.comment(config, "Filling customer phone: " + phone);
        fillText(phoneField, phone, "Phone field");
        return this;
    }

    public PaymentPage clickCheckout()
    {
        Log.comment(config, "Clicking Checkout button");
        click(checkoutButton, "Checkout button");
        return new PaymentPage(config);
    }

    public PaymentPage fillAndCheckout(String name, String phone)
    {
        fillName(name);
        fillPhone(phone);
        return clickCheckout();
    }
}
