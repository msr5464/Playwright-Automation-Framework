package automation.modules.toolshop.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class HomePage extends BasePage {
    private final Locator searchInput;
    private final Locator searchButton;
    public final Locator signInLink;

    public HomePage(Config config) {
        super(config);
        searchInput = page.locator("[data-test='search-query']");
        searchButton = page.locator("[data-test='search-submit']");
        signInLink = page.locator("[data-test='nav-sign-in']");
        assertPageLoaded(searchInput);
    }

    public ProductListPage searchProduct(String productName) {
        fillText(searchInput, productName, "Search input");
        click(searchButton, "Search button");
        return new ProductListPage(config);
    }
}
