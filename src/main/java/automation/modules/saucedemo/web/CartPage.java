package automation.modules.saucedemo.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class CartPage extends BasePage
{
    private final Locator pageTitle;
    private final Locator cartItems;
    private final Locator continueShoppingButton;

    public CartPage(Config config)
    {
        super(config);
        pageTitle              = page.locator(".title");
        cartItems              = page.locator(".cart_item");
        continueShoppingButton = page.locator("[data-test='continue-shopping']");
        assertPageLoaded(pageTitle);
    }

    public int getCartItemCount()
    {
        return cartItems.count();
    }

    public boolean isProductInCart(String productName)
    {
        Locator item = page.locator(".inventory_item_name:has-text('" + productName + "')");
        return isElementDisplayed(item);
    }

    public void removeProduct(String productName)
    {
        String dataTestId = "remove-" + productName.toLowerCase().replace(" ", "-");
        Locator removeButton = page.locator("[data-test='" + dataTestId + "']");
        click(removeButton, "Remove: " + productName);
    }

    public ProductsPage continueShopping()
    {
        click(continueShoppingButton, "Continue shopping button");
        return new ProductsPage(config);
    }
}
