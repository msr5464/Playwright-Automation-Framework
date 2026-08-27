package automation.saucedemo;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.DataGenerator;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.saucedemo.SauceDemoHelper;
import automation.modules.saucedemo.web.CartPage;
import automation.modules.saucedemo.web.CheckoutCompletePage;
import automation.modules.saucedemo.web.CheckoutInfoPage;
import automation.modules.saucedemo.web.CheckoutOverviewPage;
import automation.modules.saucedemo.web.ProductsPage;

import java.util.Map;

public class SauceDemoWebTest extends TestBase
{

    @Test(description = "verify user can login and products page loads", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loginAndVerifyProductsPage(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> credentials = sauceDemo.getCredentials("login");

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
        Map<String, String> credentials = sauceDemo.getCredentials("add_to_cart");

        config.logStep("Login to SauceDemo and add Sauce Labs Backpack to cart");
        ProductsPage products = sauceDemo.doLogin(credentials);
        products.addProductToCart("sauce-labs-backpack");

        config.logStep("Verify cart badge shows 1 item");
        AssertHelper.assertEquals(config, products.getCartCount(), "1", "Cart badge should show 1 after adding a product");
    }

    @Test(enabled=false, description = "verify cart contains the product that was added", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void verifyProductAppearsInCart(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> credentials = sauceDemo.getCredentials("verify_cart");

        config.logStep("Login, add Sauce Labs Bike Light to cart, and navigate to cart");
        ProductsPage products = sauceDemo.doLogin(credentials);
        products.addProductToCart("sauce-labs-bike-light");
        CartPage cart = products.goToCart();

        config.logStep("Verify Sauce Labs Bike Light is present in the cart");
        AssertHelper.assertTrue(config, cart.getCartItemCount() > 0, "Cart should contain at least one item");
        AssertHelper.assertTrue(config, cart.isProductInCart("Sauce Labs Bike Light"), "Bike Light should be in cart");
    }

    @Test(description = "E2E: login, sort by price low-to-high, add fleece jacket, checkout and confirm order", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void sortAndValidateProductOrderAndCheckout(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> credentials = sauceDemo.getCredentials("login");

        config.logStep("Login to SauceDemo and capture the first product name before sorting");
        ProductsPage products = sauceDemo.doLogin(credentials);
        String originalFirstProduct = products.getFirstProductName();

        config.logStep("Sort products by Price (low to high) and verify Sauce Labs Onesie is first");
        products.sortBy("lohi");
        String sortedFirstProduct = products.getFirstProductName();
        AssertHelper.assertEquals(config, sortedFirstProduct, "Sauce Labs Onesie", "First product after sorting by low price should be Sauce Labs Onesie");
        AssertHelper.assertTrue(config, !sortedFirstProduct.equals(originalFirstProduct), "Sorting should change the first product from the original default order");

        config.logStep("Verify Add to cart button is visible for Sauce Labs Fleece Jacket, add it, and verify button state changes");
        AssertHelper.assertTrue(config, products.isAddToCartButtonVisible("sauce-labs-fleece-jacket"), "Add to cart button should be visible for Sauce Labs Fleece Jacket before adding");
        products.addProductToCart("sauce-labs-fleece-jacket");
        AssertHelper.assertTrue(config, !products.isAddToCartButtonVisible("sauce-labs-fleece-jacket"), "Add to cart button should no longer be visible after adding Sauce Labs Fleece Jacket");
        AssertHelper.assertTrue(config, products.isRemoveButtonVisible("sauce-labs-fleece-jacket"), "Remove button should be visible after adding Sauce Labs Fleece Jacket to cart");

        config.logStep("Store Fleece Jacket price and navigate to cart");
        String expectedPrice = products.getProductPrice("Sauce Labs Fleece Jacket");
        CartPage cart = products.goToCart();

        config.logStep("Proceed to checkout and fill in shipping information with random data");
        CheckoutInfoPage checkoutInfo = cart.clickCheckout();
        String firstName = DataGenerator.randomAlphaString(6);
        String lastName = DataGenerator.randomAlphaString(8);
        String zipCode = DataGenerator.randomAlphaNumericString(5);
        checkoutInfo.fillCheckoutInfo(firstName, lastName, zipCode);
        CheckoutOverviewPage checkoutOverview = checkoutInfo.clickContinue();

        config.logStep("Verify Sauce Labs Fleece Jacket appears in checkout overview with the correct price");
        AssertHelper.assertTrue(config, checkoutOverview.isProductInOverview("Sauce Labs Fleece Jacket"), "Sauce Labs Fleece Jacket should appear in the checkout overview");
        AssertHelper.assertEquals(config, checkoutOverview.getProductPrice("Sauce Labs Fleece Jacket"), expectedPrice, "Product price in checkout overview should match price from product listing page");

        config.logStep("Finish order and verify confirmation header");
        CheckoutCompletePage checkoutComplete = checkoutOverview.clickFinish();
        AssertHelper.assertEquals(config, checkoutComplete.getConfirmationHeader(), "Thank you for your order!", "Order confirmation header should read 'Thank you for your order!'");
    }
}
