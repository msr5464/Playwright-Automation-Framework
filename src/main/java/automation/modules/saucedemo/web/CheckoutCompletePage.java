package automation.modules.saucedemo.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class CheckoutCompletePage extends BasePage
{
    private final Locator pageTitle;
    private final Locator confirmationHeader;
    private final Locator confirmationMessage;
    private final Locator backHomeButton;
    private final Locator ponyExpressImage;

    public CheckoutCompletePage(Config config)
    {
        super(config);
        pageTitle           = page.locator(".title");
        confirmationHeader  = page.locator("[data-test='complete-header']");
        confirmationMessage = page.locator("[data-test='complete-text']");
        backHomeButton      = page.locator("[data-test='back-to-products']");
        ponyExpressImage    = page.locator("[data-test='pony-express']");
        assertPageLoaded(confirmationHeader);
    }

    /**
     * Returns the order confirmation header text (e.g. "Thank you for your order!").
     */
    public String getConfirmationHeader()
    {
        return getText(confirmationHeader, "Confirmation header");
    }

    /**
     * Returns the order confirmation body message text.
     */
    public String getConfirmationMessage()
    {
        return getText(confirmationMessage, "Confirmation message");
    }

    /**
     * Clicks the Back Home button and returns to the Products page.
     */
    public ProductsPage clickBackHome()
    {
        click(backHomeButton, "Back Home button");
        return new ProductsPage(config);
    }
}
