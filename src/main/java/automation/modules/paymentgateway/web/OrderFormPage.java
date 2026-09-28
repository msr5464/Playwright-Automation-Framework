package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.modules.paymentgateway.PaymentGatewayData;

public class OrderFormPage extends BasePage
{
    private final Locator nameField;
    private final Locator phoneField;
    private final Locator amountText;
    private final Locator checkoutButton;

    public OrderFormPage(Config config)
    {
        super(config);
        nameField = page.locator(".cart-section:has(input[type='email']) tr:nth-child(1) input");
        phoneField = page.locator(".cart-section:has(input[type='email']) tr:nth-child(3) input");
        amountText = page.locator("td.amount");
        checkoutButton = page.locator(".cart-checkout");
        assertPageLoaded(nameField);
    }

    public OrderFormPage fillDummyOrderDetails(PaymentGatewayData data)
    {
        Log.comment(config, "Filling order form with customer name: " + data.getCustomerName() + " and phone: " + data.getCustomerPhone());
        fillText(nameField, data.getCustomerName(), "Customer name field");
        fillText(phoneField, data.getCustomerPhone(), "Customer phone field");
        return this;
    }

    public String getEnteredName()
    {
        return getInputValue(nameField, "Customer name field");
    }

    public String getEnteredPhone()
    {
        return getInputValue(phoneField, "Customer phone field");
    }

    public String getDisplayedAmount()
    {
        return getText(amountText, "Order amount");
    }

    public PaymentPopupPage clickCheckout()
    {
        Log.comment(config, "Clicking Checkout button to proceed to payment");
        click(checkoutButton, "Checkout button");
        return new PaymentPopupPage(config);
    }
}
