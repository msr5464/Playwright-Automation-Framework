package automation.saucedemo;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.saucedemo.SauceDemoData;
import automation.modules.saucedemo.SauceDemoHelper;
import automation.modules.saucedemo.web.CartPage;
import automation.modules.saucedemo.web.LoginPage;
import automation.modules.saucedemo.web.ProductDetailsPage;
import automation.modules.saucedemo.web.ProductsPage;

import java.util.Map;

public class SauceDemoWebTest extends TestBase
{

    @Test(description = "Verify a complete shopping lifecycle: login, add multiple items, remove items, and logout", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void simulateWebLifecycle(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> user = sauceDemo.getUser("standard");
        Map<String, String> product1 = sauceDemo.getProduct("backpack");
        Map<String, String> product2 = sauceDemo.getProduct("bike_light");

        config.logStep("Step 1: Login to SauceDemo and verify products page loads");
        ProductsPage products = sauceDemo.doLogin(user);
        AssertHelper.assertEquals(config, products.getPageTitle(), "Products", "User should be on Products page");

        config.logStep("Step 2: Add multiple items to the cart");
        products.addProductToCart(product1.get("slug"));
        products.addProductToCart(product2.get("slug"));
        
        config.logStep("Step 3: Verify the cart badge count reflects 2 items");
        AssertHelper.assertEquals(config, products.getCartCount(), "2", "Cart badge should display 2 items");

        config.logStep("Step 4: Navigate to the cart and verify both products are present");
        CartPage cart = products.goToCart();
        AssertHelper.assertEquals(config, cart.getCartItemCount(), 2, "Cart should contain exactly 2 items");
        AssertHelper.assertTrue(config, cart.isProductInCart(product1.get("title")), "First product should be in cart");
        AssertHelper.assertTrue(config, cart.isProductInCart(product2.get("title")), "Second product should be in cart");

        config.logStep("Step 5: Remove one product from the cart and verify count updates");
        cart.removeProduct(product2.get("title"));
        AssertHelper.assertEquals(config, cart.getCartItemCount(), 1, "Cart should contain exactly 1 item after removal");
        AssertHelper.assertFalse(config, cart.isProductInCart(product2.get("title")), "Removed product should no longer be in cart");

        config.logStep("Step 6: Continue shopping to return to products page");
        ProductsPage returnedProductsPage = cart.continueShopping();
        AssertHelper.assertEquals(config, returnedProductsPage.getPageTitle(), "Products", "User should be returned to Products page");
        AssertHelper.assertEquals(config, returnedProductsPage.getCartCount(), "1", "Cart badge should still display 1 item");

        config.logStep("Step 7: Perform logout and verify user is redirected to login page");
        LoginPage loginPage = returnedProductsPage.logout();
        AssertHelper.assertTrue(config, loginPage.isLoginPageLoaded(), "User should be redirected back to the login page after logging out");
    }

    @Test(description = "Verify a hybrid API and Web UI flow: Fetch post details via API, login, and verify product visibility", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void simulateHybridApiAndWebLifecycle(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> postData = sauceDemo.getPostData("get_post");
        int postId = Integer.parseInt(postData.get("postId"));
        
        config.logStep("Step 1 (API): Fetch mock blog post details via public JSONPlaceholder API");
        SauceDemoData post = sauceDemo.getPost(postId);
        AssertHelper.assertNotNull(config, post.getId(), "API should return a valid Post ID");
        AssertHelper.assertEquals(config, post.getId(), postId, "API should return post with ID " + postId);
        
        config.logStep("Step 2 (API): Verify the fetched post contains an expected title");
        AssertHelper.assertTrue(config, post.getTitle().contains("sunt aut facere"), "API post title should contain expected partial text");
        
        // At this point we transition over to the Web UI
        Map<String, String> user = sauceDemo.getUser("standard");
        Map<String, String> product = sauceDemo.getProduct("fleece_jacket");

        config.logStep("Step 3 (Web): Login to SauceDemo utilizing UI credentials");
        ProductsPage products = sauceDemo.doLogin(user);

        config.logStep("Step 4 (Web): Verify that the Web UI loaded correctly alongside our API execution context");
        AssertHelper.assertEquals(config, products.getPageTitle(), "Products", "User should be on Products page");
        AssertHelper.assertTrue(config, products.getProductCount() > 0, "Products page should display multiple products after loading");
        
        config.logStep("Step 5 (Web): Proceed with UI interactions, adding the " + product.get("slug") + " to the cart");
        products.addProductToCart(product.get("slug"));
        AssertHelper.assertEquals(config, products.getCartCount(), "1", "Cart badge should display 1 item added from Web UI");
        
        config.logStep("Step 6 (API): Perform a cleanup / teardown step using the API (Delete the previously fetched post)");
        io.restassured.response.Response deleteResp = sauceDemo.deletePost(postId);
        AssertHelper.assertEquals(config, deleteResp.getStatusCode(), 200, "API should confirm mock deletion with 200 OK");
    }

    @Test(description = "Verify user can login and products page loads", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loginAndVerifyProductsPage(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> user = sauceDemo.getUser("standard");

        config.logStep("Login to SauceDemo and verify products page loads with items");
        ProductsPage products = sauceDemo.doLogin(user);

        AssertHelper.assertEquals(config, products.getPageTitle(), "Products", "Products page title should be 'Products'");
        AssertHelper.assertTrue(config, products.getProductCount() > 0, "Products page should display at least one product");
    }

    @Test(description = "Verify user can add a product to cart", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void addProductToCart(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> user = sauceDemo.getUser("standard");
        Map<String, String> product = sauceDemo.getProduct("backpack");

        config.logStep("Login to SauceDemo and add " + product.get("slug") + " to cart");
        ProductsPage products = sauceDemo.doLogin(user);
        products.addProductToCart(product.get("slug"));

        config.logStep("Verify cart badge shows 1 item");
        AssertHelper.assertEquals(config, products.getCartCount(), "1", "Cart badge should show 1 after adding a product");
    }

    @Test(description = "Verify cart contains the product that was added", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void verifyProductAppearsInCart(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> user = sauceDemo.getUser("standard");
        Map<String, String> product = sauceDemo.getProduct("bike_light");

        config.logStep("Login, add " + product.get("slug") + " to cart, and navigate to cart");
        ProductsPage products = sauceDemo.doLogin(user);
        products.addProductToCart(product.get("slug"));
        CartPage cart = products.goToCart();

        config.logStep("Verify " + product.get("slug") + " is present in the cart");
        AssertHelper.assertTrue(config, cart.getCartItemCount() > 0, "Cart should contain at least one item");
        
        String expectedTitle = product.get("title");
        AssertHelper.assertTrue(config, cart.isProductInCart(expectedTitle), expectedTitle + " should be in cart");
    }

    @Test(description = "Login as standard user, open the Sauce Labs Backpack details page, verify name and price, add to cart, verify button changes to Remove and badge shows 1, go back to products and verify badge persists", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void addToCartFromDetailsPage(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> user = sauceDemo.getUser("standard");
        Map<String, String> product = sauceDemo.getProduct("backpack");

        config.logStep("Login to SauceDemo as standard user and land on the Products page");
        ProductsPage products = sauceDemo.doLogin(user);

        config.logStep("Verify the Products page title is 'Products'");
        AssertHelper.assertEquals(config, products.getPageTitle(), "Products", "User should be on Products page");

        config.logStep("Open the Sauce Labs Backpack product details page");
        ProductDetailsPage details = products.clickProductByName(product.get("title"));

        config.logStep("Verify the product name on the details page is 'Sauce Labs Backpack'");
        AssertHelper.assertEquals(config, details.getProductName(), product.get("title"), "Product name on details page should match");

        config.logStep("Verify the product price on the details page is '$29.99'");
        AssertHelper.assertEquals(config, details.getProductPrice(), "$29.99", "Product price should be $29.99");

        config.logStep("Add the product to cart from the details page");
        details.addToCart();

        config.logStep("Verify the Remove button is displayed after adding the product to cart");
        AssertHelper.assertTrue(config, details.isRemoveButtonDisplayed(), "Remove button should be displayed after adding to cart");

        config.logStep("Verify the cart badge shows 1 item after adding the product");
        AssertHelper.assertEquals(config, details.getCartCount(), "1", "Cart badge should show 1 after adding product from details page");

        config.logStep("Navigate back to the Products page");
        ProductsPage returnedProducts = details.backToProducts();

        config.logStep("Verify the Products page title is 'Products' after navigating back");
        AssertHelper.assertEquals(config, returnedProducts.getPageTitle(), "Products", "User should be back on Products page");

        config.logStep("Verify the cart badge still shows 1 item on the Products page");
        AssertHelper.assertEquals(config, returnedProducts.getCartCount(), "1", "Cart badge should still show 1 after returning to products");
    }
}
