package automation.modules.saucedemo.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

public class ProductsPage extends BasePage
{
    private final Locator inventory;
    private final Locator pageTitle;
    private final Locator cartLink;
    private final Locator menuButton;
    private final Locator logoutLink;

    public ProductsPage(Config config)
    {
        super(config);
        inventory   = page.locator("[data-test='inventory-container']");
        pageTitle   = page.locator(".title");
        cartLink    = page.locator(".shopping_cart_link");
        menuButton  = page.locator("#react-burger-menu-btn");
        logoutLink  = page.locator("#logout_sidebar_link");
        // Not pageTitle: the cart page has a .title too, so a ProductsPage built after
        // "Continue shopping" would pass this check before the cart page is gone.
        assertPageLoaded(inventory);
    }

    public String getPageTitle()
    {
        return getText(pageTitle, "Page title");
    }

    public int getProductCount()
    {
        return page.locator(".inventory_item").count();
    }

    public void addProductToCart(String productName)
    {
        String dataTestId = "add-to-cart-" + productName.toLowerCase().replace(" ", "-");
        Locator addButton = page.locator("[data-test='" + dataTestId + "']");
        click(addButton, "Add to cart: " + productName);
    }

    public void removeProductFromCart(String productName)
    {
        String dataTestId = "remove-" + productName.toLowerCase().replace(" ", "-");
        Locator removeButton = page.locator("[data-test='" + dataTestId + "']");
        click(removeButton, "Remove from cart: " + productName);
    }

    public String getCartCount()
    {
        Locator badge = page.locator(".shopping_cart_badge");
        if (!isElementDisplayed(badge)) return "0";
        return getText(badge, "Cart badge count");
    }

    public CartPage goToCart()
    {
        click(cartLink, "Shopping cart link");
        return new CartPage(config);
    }

    public LoginPage logout()
    {
        click(menuButton, "Burger menu");
        WaitHelper.waitForElementToBeVisible(config, logoutLink, "Logout link");
        click(logoutLink, "Logout link");
        return new LoginPage(config);
    }
}
