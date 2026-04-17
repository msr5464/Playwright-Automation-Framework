package automation.modules.saucedemo.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

import java.util.List;

public class ProductsPage extends BasePage
{
    private final Locator pageTitle;
    private final Locator cartLink;
    private final Locator menuButton;
    private final Locator logoutLink;
    private final Locator sortDropdown;
    private final Locator inventoryItemNames;
    private final Locator inventoryItemPrices;

    public ProductsPage(Config config)
    {
        super(config);
        pageTitle          = page.locator(".title");
        cartLink           = page.locator(".shopping_cart_link");
        menuButton         = page.locator("#react-burger-menu-btn");
        logoutLink         = page.locator("#logout_sidebar_link");
        sortDropdown       = page.locator("[data-test='product-sort-container']");
        inventoryItemNames = page.locator(".inventory_item_name");
        inventoryItemPrices = page.locator(".inventory_item_price");
        assertPageLoaded(pageTitle);
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

    public void logout()
    {
        click(menuButton, "Burger menu");
        WaitHelper.waitForElementToBeVisible(config, logoutLink, "Logout link");
        click(logoutLink, "Logout link");
    }

    /**
     * Returns the name of the first product displayed in the product list.
     */
    public String getFirstProductName()
    {
        return getText(inventoryItemNames.first(), "First product name");
    }

    /**
     * Returns the price of the first product displayed in the product list.
     */
    public String getFirstProductPrice()
    {
        return getText(inventoryItemPrices.first(), "First product price");
    }

    /**
     * Sorts the product list using the sort dropdown.
     * Valid values: "az", "za", "lohi", "hilo"
     */
    public void sortBy(String value)
    {
        selectOption(sortDropdown, value, "Sort dropdown");
    }

    /**
     * Returns the display names of all products currently shown in the list.
     */
    public List<String> getAllProductNames()
    {
        return inventoryItemNames.allTextContents();
    }

    /**
     * Returns true if the Add to cart button is visible for the given product slug.
     * Accepts either the slug form ("sauce-labs-fleece-jacket") or display name ("Sauce Labs Fleece Jacket").
     */
    public boolean isAddToCartButtonVisible(String productName)
    {
        String dataTestId = "add-to-cart-" + productName.toLowerCase().replace(" ", "-");
        Locator button = page.locator("[data-test='" + dataTestId + "']");
        return isElementDisplayed(button);
    }

    /**
     * Returns true if the Remove button is visible for the given product slug.
     * Accepts either the slug form ("sauce-labs-fleece-jacket") or display name ("Sauce Labs Fleece Jacket").
     */
    public boolean isRemoveButtonVisible(String productName)
    {
        String dataTestId = "remove-" + productName.toLowerCase().replace(" ", "-");
        Locator button = page.locator("[data-test='" + dataTestId + "']");
        return isElementDisplayed(button);
    }

    /**
     * Returns the displayed price for the product with the given display name.
     */
    public String getProductPrice(String productName)
    {
        Locator priceLocator = page.locator(
            ".inventory_item:has(.inventory_item_name:has-text('" + productName + "')) .inventory_item_price");
        return getText(priceLocator, "Price for " + productName);
    }
}
