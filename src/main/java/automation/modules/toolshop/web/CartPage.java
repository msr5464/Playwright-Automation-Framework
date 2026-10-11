package automation.modules.toolshop.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.modules.toolshop.ToolshopHelper;

public class CartPage extends BasePage
{
    private final Locator productNameDisplay;
    private final Locator quantityDisplay;
    private final Locator priceDisplay;
    private final Locator totalDisplay;
    private final Locator proceedToCheckoutButton;

    public CartPage(Config config)
    {
        super(config);
        productNameDisplay = page.locator("[data-test='product-title']");
        quantityDisplay = page.locator("[data-test='product-quantity']");
        priceDisplay = page.locator("[data-test='product-price']");
        totalDisplay = page.locator("[data-test='cart-total']");
        proceedToCheckoutButton = page.locator("[data-test='proceed-1']");
        assertPageLoaded(productNameDisplay);
    }

    public String getProductName()
    {
        return getText(productNameDisplay, "Product name").replace('\u00A0', ' ');
    }

    public String getQuantity()
    {
        return getText(quantityDisplay, "Product quantity");
    }

    public String getPrice()
    {
        return ToolshopHelper.toPlainAmount(getText(priceDisplay, "Product price"));
    }

    public String getTotal()
    {
        return getText(totalDisplay, "Cart total");
    }

    public CheckoutSignInPage proceedToCheckout()
    {
        click(proceedToCheckoutButton, "Proceed to checkout button");
        return new CheckoutSignInPage(config);
    }
}
