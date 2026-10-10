package automation.modules.saucedemo;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.TestDataReader;
import automation.core.api.ApiHelper;
import automation.modules.saucedemo.web.CartPage;
import automation.modules.saucedemo.web.LoginPage;
import automation.modules.saucedemo.web.ProductsPage;
import automation.modules.saucedemo.api.SauceDemoApi;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Unified helper for SauceDemo web flows and JSONPlaceholder API flows.
 * Extends ApiHelper with the JSONPlaceholder base URL (external API — no app auth).
 * Web credentials are loaded from users.csv, not the user pool.
 *
 * API usage:
 *   SauceDemoHelper api = new SauceDemoHelper(config);
 *   SauceDemoData created = api.execute(SauceDemoApi.CreatePost, post, SauceDemoData.class);
 *   SauceDemoData fetched = api.execute(SauceDemoApi.GetPost.withPath("id", "1"), SauceDemoData.class);
 *   api.execute(SauceDemoApi.DeletePost.withPath("id", "1"));
 *
 * Web usage — the Helper holds one public field per page. Every action that leaves a
 * page returns the next page object, and the test stores it on that page's field, so
 * each step continues from the page the previous step returned:
 *   SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
 *   sauceDemo.productsPage = sauceDemo.doLogin(sauceDemo.getUser("standard"));
 *   sauceDemo.productsPage = sauceDemo.addToCart(sauceDemo.getProduct("backpack"));
 *   sauceDemo.cartPage = sauceDemo.productsPage.goToCart();
 *   sauceDemo.productsPage = sauceDemo.cartPage.continueShopping();
 */
public class SauceDemoHelper extends ApiHelper
{
    private static final String API_BASE_URL = "https://jsonplaceholder.typicode.com";

    public LoginPage loginPage;
    public ProductsPage productsPage;
    public CartPage cartPage;

    public SauceDemoHelper(Config config)
    {
        super(config, API_BASE_URL);
    }

    public ProductsPage doLogin(Map<String, String> credentials)
    {
        return doLogin(credentials.get("username"), credentials.get("password"));
    }

    /**
     * Open SauceDemo and log in. The entry operation: the one place a page object is
     * constructed, right after navigating. Returns the Products page it lands on.
     */
    public ProductsPage doLogin(String username, String password)
    {
        String url = config.getRunTimeProperty("saucedemo.url");
        Log.comment(config, "Navigating to SauceDemo: " + url);
        BrowserHelper.navigateTo(config, url);
        loginPage = new LoginPage(config);
        return loginPage.doLogin(username, password);
    }

    /**
     * Add each product to the cart by its products.csv row, continuing from the
     * Products page the previous step stored in productsPage. Returns that page, so
     * the test can check the cart badge next.
     */
    @SafeVarargs
    public final ProductsPage addToCart(Map<String, String>... products)
    {
        for (Map<String, String> product : products)
        {
            productsPage.addProductToCart(product.get("slug"));
        }
        return productsPage;
    }

    /**
     * Add the products to the cart and open it, for a test that checks nothing in
     * between. Returns the Cart page it lands on.
     */
    @SafeVarargs
    public final CartPage openCartWith(Map<String, String>... products)
    {
        cartPage = addToCart(products).goToCart();
        return cartPage;
    }

    /**
     * Load user credentials by user_key from users.csv.
     * CSV: src/test/resources/saucedemo/csvFiles/users.csv
     */
    public Map<String, String> getUser(String userKey)
    {
        return TestDataReader.loadCsvRowByColumnValue(
            "saucedemo", "users", "user_key", userKey, Config.environment);
    }

    /**
     * Load product information by product_key from products.csv.
     * CSV: src/test/resources/saucedemo/csvFiles/products.csv
     */
    public Map<String, String> getProduct(String productKey)
    {
        return TestDataReader.loadCsvRowByColumnValue(
            "saucedemo", "products", "product_key", productKey, Config.environment);
    }

    /**
     * Load post test data by post_key from posts.csv.
     * CSV: src/test/resources/saucedemo/csvFiles/posts.csv
     */
    public Map<String, String> getPostData(String postKey)
    {
        return TestDataReader.loadCsvRowByColumnValue(
            "saucedemo", "posts", "post_key", postKey, Config.environment);
    }

    // ========== API HELPERS ==========

    public SauceDemoData[] getAllPosts(int limit)
    {
        Log.comment(config, "Fetching posts via API with query parameter limit=" + limit);
        return execute(SauceDemoApi.ListPosts.withQueryParam("_limit", String.valueOf(limit)), SauceDemoData[].class);
    }

    public SauceDemoData[] getAllPosts()
    {
        Log.comment(config, "Fetching all posts via API");
        return execute(SauceDemoApi.ListPosts, SauceDemoData[].class);
    }

    public SauceDemoData getPost(int postId)
    {
        Log.comment(config, "Fetching post " + postId + " via API");
        return execute(SauceDemoApi.GetPost.withPath("id", String.valueOf(postId)), SauceDemoData.class);
    }

    public Response getPostRaw(int postId)
    {
        Log.comment(config, "Fetching post " + postId + " via API (raw)");
        return executeRaw(SauceDemoApi.GetPost.withPath("id", String.valueOf(postId)), null);
    }

    public SauceDemoData createPost(int userId, String title, String body)
    {
        Log.comment(config, "Creating post via API - userId: " + userId + ", title: " + title);
        SauceDemoData request = new SauceDemoBuilder()
            .withUserId(userId)
            .withTitle(title)
            .withBody(body)
            .build();
            
        // Demonstrate passing custom headers / Auth token during an execute call
        Map<String, String> headers = Map.of(
            "Authorization", "Bearer mock-auth-token-12345",
            "X-Custom-Header", "JarvisAutomation"
        );
        
        return execute(SauceDemoApi.CreatePost, request, headers, SauceDemoData.class);
    }

    public SauceDemoData updatePost(int postId, int userId, String title, String body)
    {
        Log.comment(config, "Updating post " + postId + " via API");
        SauceDemoData request = new SauceDemoBuilder()
            .withUserId(userId)
            .withTitle(title)
            .withBody(body)
            .build();
        return execute(SauceDemoApi.UpdatePost.withPath("id", String.valueOf(postId)), request, SauceDemoData.class);
    }

    public Response deletePost(int postId)
    {
        Log.comment(config, "Deleting post " + postId + " via API");
        return executeRaw(SauceDemoApi.DeletePost.withPath("id", String.valueOf(postId)), null);
    }
}
