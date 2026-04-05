package automation.saucedemo;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.saucedemo.SauceDemoHelper;
import automation.modules.saucedemo.web.CartPage;
import automation.modules.saucedemo.web.ProductsPage;

import java.util.Map;

public class SauceDemoWebTest extends TestBase
{

    @Test(description = "verify user can login and products page loads", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loginAndVerifyProductsPage(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> credentials = sauceDemo.getSauceDemoCredentials("login");

        config.logStep("Login to SauceDemo and verify products page loads with items");
        ProductsPage products = sauceDemo.doLogin(credentials);

        AssertHelper.assertEquals(config, products.getPageTitle(), "Products", "Products page title should be 'Products'");
        AssertHelper.assertTrue(config, products.getProductCount() > 0, "Products page should display at least one product");
    }

    @Test(description = "verify user can add a product to cart", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void addProductToCart(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> credentials = sauceDemo.getSauceDemoCredentials("add_to_cart");

        config.logStep("Login to SauceDemo and add Sauce Labs Backpack to cart");
        ProductsPage products = sauceDemo.doLogin(credentials);
        products.addProductToCart("sauce-labs-backpack");

        config.logStep("Verify cart badge shows 1 item");
        AssertHelper.assertEquals(config, products.getCartCount(), "1", "Cart badge should show 1 after adding a product");
    }

    @Test(description = "verify cart contains the product that was added", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void verifyProductAppearsInCart(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> credentials = sauceDemo.getSauceDemoCredentials("verify_cart");

        config.logStep("Login, add Sauce Labs Bike Light to cart, and navigate to cart");
        ProductsPage products = sauceDemo.doLogin(credentials);
        products.addProductToCart("sauce-labs-bike-light");
        CartPage cart = products.goToCart();

        config.logStep("Verify Sauce Labs Bike Light is present in the cart");
        AssertHelper.assertTrue(config, cart.getCartItemCount() > 0, "Cart should contain at least one item");
        AssertHelper.assertTrue(config, cart.isProductInCart("Sauce Labs Bike Light"), "Bike Light should be in cart");
    }

    @Test(description = "verify product can be removed from cart", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void removeProductFromCart(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> credentials = sauceDemo.getSauceDemoCredentials("remove_product");

        config.logStep("Login, add Sauce Labs Backpack to cart, then navigate to cart");
        ProductsPage products = sauceDemo.doLogin(credentials);
        products.addProductToCart("sauce-labs-backpack");
        CartPage cart = products.goToCart();

        config.logStep("Remove the product from cart and verify cart is empty");
        cart.removeProduct("sauce-labs-backpack");
        AssertHelper.assertEquals(config, cart.getCartItemCount(), 0, "Cart should be empty after removing the product");
    }
}
