package automation.modules.saucedemo.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class ProductDetailsPage extends BasePage
{
    private final Locator productName;
    private final Locator productPrice;
    private final Locator addToCartButton;
    private final Locator removeButton;
    private final Locator backToProductsButton;
    private final Locator cartBadge;

    public ProductDetailsPage(Config config)
    {
        super(config);
        productName          = page.locator("[data-test='inventory-item-name']");
        productPrice         = page.locator("[data-test='inventory-item-price']");
        addToCartButton      = page.locator("[data-test='add-to-cart']");
        removeButton         = page.locator("[data-test='remove']");
        backToProductsButton = page.locator("[data-test='back-to-products']");
        cartBadge            = page.locator("[data-test='shopping-cart-badge']");
        assertPageLoaded(backToProductsButton);
    }

    public String getProductName()
    {
        return getText(productName, "Product name");
    }

    public String getProductPrice()
    {
        return getText(productPrice, "Product price");
    }

    public void addToCart()
    {
        Log.comment(config, "Adding product to cart from details page");
        click(addToCartButton, "Add to cart button");
    }

    public boolean isRemoveButtonDisplayed()
    {
        return isElementDisplayed(removeButton);
    }

    public String getCartCount()
    {
        if (!isElementDisplayed(cartBadge)) return "0";
        return getText(cartBadge, "Cart badge");
    }

    public ProductsPage backToProducts()
    {
        Log.comment(config, "Navigating back to products page");
        click(backToProductsButton, "Back to products button");
        return new ProductsPage(config);
    }
}
