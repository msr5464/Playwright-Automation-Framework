package automation.modules.toolshop.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;
import automation.modules.toolshop.ToolshopHelper;

public class ProductDetailsPage extends BasePage {
    private final Locator productNameDisplay;
    private final Locator priceDisplay;
    private final Locator quantityInput;
    private final Locator increaseQuantityButton;
    private final Locator addToCartButton;
    private final Locator toastMessage;
    private final Locator cartIcon;

    public ProductDetailsPage(Config config) {
        super(config);
        productNameDisplay = page.locator("h1");
        priceDisplay = page.locator("[data-test='unit-mukesh']");
        quantityInput = page.locator("[data-test='quantity']");
        increaseQuantityButton = page.locator("[data-test='increase-quantity']");
        addToCartButton = page.locator("[data-test='add-to-cart']");
        toastMessage = page.locator(".toast-mukesh");
        cartIcon = page.locator("[data-test='cart-quantity']");
        assertPageLoaded(productNameDisplay);
    }

    public String getProductName() {
        return getText(productNameDisplay, "Product name");
    }

    public String getPrice() {
        return ToolshopHelper.toPlainAmount(getText(priceDisplay, "Unit price"));
    }

    public ProductDetailsPage increaseQuantity() {
        click(increaseQuantityButton, "Increase quantity button");
        WaitHelper.waitForPageToSettle(config);
        return this;
    }

    public ProductDetailsPage addToCart() {
        click(addToCartButton, "Add to cart button");
        WaitHelper.waitForPageToSettle(config);
        return this;
    }

    public String getToastMessage() {
        return getText(toastMessage, "Toast message");
    }

    public String getCartCount() {
        return getText(cartIcon, "Cart count");
    }

    public CartPage openCart() {
        click(cartIcon, "Cart icon");
        return new CartPage(config);
    }
}
