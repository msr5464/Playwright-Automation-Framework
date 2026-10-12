package automation.modules.toolshop.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class ProductListPage extends BasePage
{
    private final Locator productCards;
    private final Locator productNameText;

    public ProductListPage(Config config)
    {
        super(config);
        productCards = page.locator("a.card");
        productNameText = page.locator("[data-test='product-name']");
        assertPageLoaded(productCards);
    }

    public int getResultsCount()
    {
        return productCards.count();
    }

    public String getFirstProductName()
    {
        return getText(productNameText.first(), "First product name");
    }

    public ProductDetailsPage openProduct(String productName)
    {
        Locator card = productCards.filter(new Locator.FilterOptions().setHasText(productName));
        click(card, "Product: " + productName);
        return new ProductDetailsPage(config);
    }
}
