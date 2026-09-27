package automation.modules.paymentpage.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.modules.paymentpage.PaymentPageData;

public class CheckoutFormPage extends BasePage
{
    private final Locator nameField;
    private final Locator emailField;
    private final Locator phoneField;
    private final Locator checkoutButton;

    public CheckoutFormPage(Config config)
    {
        super(config);
        nameField      = page.locator(".cart-inner .cart-section:last-child tr:nth-child(1) input");
        emailField     = page.locator(".cart-inner .cart-section:last-child tr:nth-child(2) input");
        phoneField     = page.locator(".cart-inner .cart-section:last-child tr:nth-child(3) input");
        checkoutButton = page.locator("div.cart-checkout");
        assertPageLoaded(nameField);
    }

    /**
     * Fills the customer details form. The site uses a single Name field,
     * so firstName and lastName are joined with a space.
     */
    public CheckoutFormPage fillCheckoutDetails(PaymentPageData data)
    {
        Log.comment(config, "Filling checkout form with customer details");
        fillText(nameField,  data.getFirstName() + " " + data.getLastName(), "Name field");
        fillText(emailField, data.getEmail(),  "Email field");
        fillText(phoneField, data.getPhone(),  "Phone field");
        return this;
    }

    public PaymentPopupPage clickCheckout()
    {
        Log.comment(config, "Clicking Checkout button");
        click(checkoutButton, "Checkout button");
        return new PaymentPopupPage(config);
    }
}
