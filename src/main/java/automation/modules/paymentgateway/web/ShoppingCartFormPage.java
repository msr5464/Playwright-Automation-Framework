package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.DataGenerator;
import automation.core.Log;

public class ShoppingCartFormPage extends BasePage
{
    private final Locator nameField;
    private final Locator phoneField;
    private final Locator amountField;
    private final Locator emailField;
    private final Locator checkoutButton;

    public ShoppingCartFormPage(Config config)
    {
        super(config);
        nameField = page.locator(".cart-section:has(input[type='email']) tr:nth-child(1) input");
        phoneField = page.locator(".cart-section:has(input[type='email']) tr:nth-child(3) input");
        amountField = page.locator("td.amount");
        emailField = page.locator(".cart-section:has(input[type='email']) input[type='email']");
        checkoutButton = page.locator(".cart-checkout");
        assertPageLoaded(nameField);
    }

    public void fillName(String name)
    {
        Log.comment(config, "Filling name field with: " + name);
        fillText(nameField, name, "Name field");
    }

    public void fillPhone(String phone)
    {
        Log.comment(config, "Filling phone field with: " + phone);
        fillText(phoneField, phone, "Phone field");
    }

    public void fillAmount(String amount)
    {
        Log.comment(config, "Filling amount field with: " + amount);
        fillText(amountField, amount, "Amount field");
    }

    public void fillRemainingFieldsWithDummyData()
    {
        Log.comment(config, "Filling remaining shopping cart fields with dummy data");
        if (isElementDisplayed(emailField))
        {
            fillText(emailField, DataGenerator.randomEmail(), "Email field");
        }
    }

    public PaymentPopupPage clickCheckout()
    {
        Log.comment(config, "Clicking Checkout button");
        click(checkoutButton, "Checkout button");
        return new PaymentPopupPage(config);
    }
}
