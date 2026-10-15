package automation.saucedemo;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.saucedemo.SauceDemoData;
import automation.modules.saucedemo.SauceDemoHelper;

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
        sauceDemo.productsPage = sauceDemo.doLogin(user);
        AssertHelper.assertEquals(config, sauceDemo.productsPage.getPageTitle(), "Products", "User should be on Products page");

        config.logStep("Step 2: Add both products to the cart and verify the cart badge shows 2 items");
        sauceDemo.productsPage = sauceDemo.addToCart(product1, product2);
        AssertHelper.assertEquals(config, sauceDemo.productsPage.getCartCount(), "2", "Cart badge should display 2 items");

        config.logStep("Step 3: Navigate to the cart and verify both products are present");
        sauceDemo.cartPage = sauceDemo.productsPage.goToCart();
        AssertHelper.assertEquals(config, sauceDemo.cartPage.getCartItemCount(), 2, "Cart should contain exactly 2 items");
        AssertHelper.assertTrue(config, sauceDemo.cartPage.isProductInCart(product1.get("title")), "First product should be in cart");
        AssertHelper.assertTrue(config, sauceDemo.cartPage.isProductInCart(product2.get("title")), "Second product should be in cart");

        config.logStep("Step 4: Remove one product from the cart and verify count updates");
        sauceDemo.cartPage.removeProduct(product2.get("title"));
        AssertHelper.assertEquals(config, sauceDemo.cartPage.getCartItemCount(), 1, "Cart should contain exactly 1 item after removal");
        AssertHelper.assertFalse(config, sauceDemo.cartPage.isProductInCart(product2.get("title")), "Removed product should no longer be in cart");

        config.logStep("Step 5: Continue shopping to return to products page");
        sauceDemo.productsPage = sauceDemo.cartPage.continueShopping();
        AssertHelper.assertEquals(config, sauceDemo.productsPage.getPageTitle(), "Products", "User should be returned to Products page");
        AssertHelper.assertEquals(config, sauceDemo.productsPage.getCartCount(), "1", "Cart badge should still display 1 item");

        config.logStep("Step 6: Perform logout and verify user is redirected to login page");
        sauceDemo.loginPage = sauceDemo.productsPage.logout();
        AssertHelper.assertTrue(config, sauceDemo.loginPage.isLoginPageLoaded(), "User should be redirected back to the login page after logging out");
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
        sauceDemo.productsPage = sauceDemo.doLogin(user);

        config.logStep("Step 4 (Web): Verify that the Web UI loaded correctly alongside our API execution context");
        AssertHelper.assertEquals(config, sauceDemo.productsPage.getPageTitle(), "Products", "User should be on Products page");
        AssertHelper.assertTrue(config, sauceDemo.productsPage.getProductCount() > 0, "Products page should display multiple products after loading");
        
        config.logStep("Step 5 (Web): Proceed with UI interactions, adding the " + product.get("slug") + " to the cart");
        sauceDemo.productsPage = sauceDemo.addToCart(product);
        AssertHelper.assertEquals(config, sauceDemo.productsPage.getCartCount(), "1", "Cart badge should display 1 item added from Web UI");
        
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
        sauceDemo.productsPage = sauceDemo.doLogin(user);

        AssertHelper.assertEquals(config, sauceDemo.productsPage.getPageTitle(), "Products", "Products page title should be 'Products'");
        AssertHelper.assertTrue(config, sauceDemo.productsPage.getProductCount() > 0, "Products page should display at least one product");
    }

    @Test(description = "Verify user can add a product to cart", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void addProductToCart(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> user = sauceDemo.getUser("standard");
        Map<String, String> product = sauceDemo.getProduct("backpack");

        config.logStep("Login to SauceDemo");
        sauceDemo.productsPage = sauceDemo.doLogin(user);

        config.logStep("Add " + product.get("slug") + " to the cart and verify the cart badge shows 1 item");
        sauceDemo.productsPage = sauceDemo.addToCart(product);
        AssertHelper.assertEquals(config, sauceDemo.productsPage.getCartCount(), "1", "Cart badge should show 1 after adding a product");
    }

    @Test(description = "Verify cart contains the product that was added", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void verifyProductAppearsInCart(Config config)
    {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> user = sauceDemo.getUser("standard");
        Map<String, String> product = sauceDemo.getProduct("bike_light");

        config.logStep("Login to SauceDemo");
        sauceDemo.productsPage = sauceDemo.doLogin(user);

        config.logStep("Add " + product.get("slug") + " to the cart, open the cart and verify it is listed");
        sauceDemo.cartPage = sauceDemo.openCartWith(product);
        AssertHelper.assertTrue(config, sauceDemo.cartPage.getCartItemCount() > 0, "Cart should contain at least one item");

        String expectedTitle = product.get("title");
        AssertHelper.assertTrue(config, sauceDemo.cartPage.isProductInCart(expectedTitle), expectedTitle + " should be in cart");
    }
}
