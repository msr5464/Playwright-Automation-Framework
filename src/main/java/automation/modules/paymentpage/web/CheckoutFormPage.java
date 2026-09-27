package automation.modules.paymentpage.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.modules.paymentpage.PaymentPageData;

public class CheckoutFormPage extends BasePage
{
    private final Locator nameField;
    private final Locator phoneField;
    private final Locator checkoutButton;

    public CheckoutFormPage(Config config)
    {
        super(config);
        nameField     = page.locator(".cart-section:last-child tr:nth-child(1) input");
        phoneField    = page.locator(".cart-section:last-child tr:nth-child(3) input");
        checkoutButton = page.locator(".cart-checkout");
        assertPageLoaded(nameField);
    }

    public CheckoutFormPage fillName(String name)
    {
        Log.comment(config, "Filling name: " + name);
        fillText(nameField, name, "Name field");
        return this;
    }

    public CheckoutFormPage fillPhone(String phone)
    {
        Log.comment(config, "Filling phone: " + phone);
        fillText(phoneField, phone, "Phone field");
        return this;
    }

    public CheckoutFormPage fillForm(PaymentPageData data)
    {
        fillName(data.getName());
        fillPhone(data.getPhone());
        return this;
    }

    public PaymentPage clickCheckout()
    {
        Log.comment(config, "Clicking Checkout button");
        click(checkoutButton, "Checkout button");
        return new PaymentPage(config);
    }
}
