package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class CheckoutFormPage extends BasePage
{
    private final Locator nameField;
    private final Locator lastNameField;
    private final Locator phoneField;
    private final Locator checkoutButton;

    public CheckoutFormPage(Config config)
    {
        super(config);
        nameField      = page.locator(".cart-section:has(input[type='email']) tr:nth-child(1) input");
        lastNameField  = page.locator(".cart-section:has(input[type='email']) tr:nth-child(2) input");
        phoneField     = page.locator(".cart-section:has(input[type='email']) tr:nth-child(3) input");
        checkoutButton = page.locator(".cart-checkout");
        assertPageLoaded(nameField);
    }

    public void fillName(String name)
    {
        Log.comment(config, "Entering customer name: " + name);
        fillText(nameField, name, "Name field");
    }

    public void fillPhone(String phone)
    {
        Log.comment(config, "Entering customer phone: " + phone);
        fillText(phoneField, phone, "Phone field");
    }

    public PaymentPage clickCheckout()
    {
        Log.comment(config, "Clicking Checkout button to proceed to payment");
        click(checkoutButton, "Checkout button");
        return new PaymentPage(config);
    }

    public PaymentPage fillAndCheckout(String name, String phone)
    {
        fillName(name);
        fillText(lastNameField, "", "Last name field");
        fillPhone(phone);
        return clickCheckout();
    }
}
