package automation.modules.saucedemo.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

import java.util.List;

public class CheckoutOverviewPage extends BasePage
{
    private final Locator pageTitle;
    private final Locator cartItemNames;
    private final Locator cartItemPrices;
    private final Locator itemTotal;
    private final Locator taxAmount;
    private final Locator orderTotal;
    private final Locator finishButton;
    private final Locator cancelButton;

    public CheckoutOverviewPage(Config config)
    {
        super(config);
        pageTitle      = page.locator(".title");
        cartItemNames  = page.locator(".inventory_item_name");
        cartItemPrices = page.locator(".inventory_item_price");
        itemTotal      = page.locator(".summary_subtotal_label");
        taxAmount      = page.locator(".summary_tax_label");
        orderTotal     = page.locator(".summary_total_label");
        finishButton   = page.locator("[data-test='finish']");
        cancelButton   = page.locator("[data-test='cancel']");
        assertPageLoaded(pageTitle);
    }

    /**
     * Returns the display names of all products in the checkout overview.
     */
    public List<String> getCartItemNames()
    {
        return cartItemNames.allTextContents();
    }

    /**
     * Returns the displayed prices of all products in the checkout overview.
     */
    public List<String> getCartItemPrices()
    {
        return cartItemPrices.allTextContents();
    }

    /**
     * Returns the item subtotal label text (e.g. "Item total: $49.99").
     */
    public String getItemTotal()
    {
        return getText(itemTotal, "Item total");
    }

    /**
     * Returns the tax label text (e.g. "Tax: $4.00").
     */
    public String getTax()
    {
        return getText(taxAmount, "Tax amount");
    }

    /**
     * Returns the order total label text (e.g. "Total: $53.99").
     */
    public String getOrderTotal()
    {
        return getText(orderTotal, "Order total");
    }

    /**
     * Returns true if the given product name appears in the checkout overview.
     */
    public boolean isProductInOverview(String productName)
    {
        Locator item = page.locator(".inventory_item_name:has-text('" + productName + "')");
        return isElementDisplayed(item);
    }

    /**
     * Returns the displayed price for the product with the given display name.
     */
    public String getProductPrice(String productName)
    {
        Locator priceLocator = page.locator(
            ".cart_item:has(.inventory_item_name:has-text('" + productName + "')) .inventory_item_price");
        return getText(priceLocator, "Price for " + productName);
    }

    /**
     * Confirms the order and navigates to the Checkout Complete page.
     */
    public CheckoutCompletePage clickFinish()
    {
        click(finishButton, "Finish button");
        return new CheckoutCompletePage(config);
    }

    /**
     * Cancels the order and returns to the Cart page.
     */
    public CartPage clickCancel()
    {
        click(cancelButton, "Cancel button");
        return new CartPage(config);
    }
}
