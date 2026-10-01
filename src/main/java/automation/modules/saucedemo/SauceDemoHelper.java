package automation.modules.saucedemo;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.TestDataReader;
import automation.core.api.ApiHelper;
import automation.modules.saucedemo.web.LoginPage;
import automation.modules.saucedemo.web.ProductsPage;
import automation.modules.saucedemo.api.SauceDemoApi;
import io.restassured.response.Response;

import java.util.Arrays;
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
 * Web usage:
 *   SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
 *   Map<String, String> user = sauceDemo.getUser("standard");
 *   ProductsPage products = sauceDemo.doLogin(user);
 *   products.addProductToCart("sauce-labs-backpack");
 *   CartPage cart = products.goToCart();
 */
public class SauceDemoHelper extends ApiHelper
{
    public SauceDemoHelper(Config config)
    {
        super(config, config.getRunTimeProperty("saucedemo.api.url"));
    }

    public ProductsPage doLogin(Map<String, String> credentials)
    {
        return doLogin(credentials.get("username"), credentials.get("password"));
    }

    public ProductsPage doLogin(String username, String password)
    {
        String url = config.getRunTimeProperty("saucedemo.url");
        Log.comment(config, "Navigating to SauceDemo: " + url);
        BrowserHelper.navigateTo(config, url);
        return new LoginPage(config).doLogin(username, password);
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

    /**
     * Load todo test data by todo_key from todos.csv.
     * CSV: src/test/resources/saucedemo/csvFiles/todos.csv
     */
    public Map<String, String> getTodoData(String todoKey)
    {
        return TestDataReader.loadCsvRowByColumnValue(
            "saucedemo", "todos", "todo_key", todoKey, Config.environment);
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

    // ========== TODO API HELPERS ==========

    public SauceDemoData createTodo(int userId, String title, boolean completed)
    {
        Log.comment(config, "Creating todo via API - userId: " + userId + ", title: " + title);
        return execute(SauceDemoApi.CreateTodo,
            map().put("userId", userId).put("title", title).put("completed", completed).build(),
            SauceDemoData.class);
    }

    public SauceDemoData getTodo(int id)
    {
        Log.comment(config, "Fetching todo " + id + " via API");
        return execute(SauceDemoApi.GetTodo.withPath("id", String.valueOf(id)), SauceDemoData.class);
    }

    public SauceDemoData replaceTodo(int id, int userId, String title, boolean completed)
    {
        Log.comment(config, "Replacing todo " + id + " via API");
        return execute(SauceDemoApi.ReplaceTodo.withPath("id", String.valueOf(id)),
            map().put("id", id).put("userId", userId).put("title", title).put("completed", completed).build(),
            SauceDemoData.class);
    }

    public SauceDemoData patchTodo(int id, boolean completed)
    {
        Log.comment(config, "Patching todo " + id + " completed=" + completed + " via API");
        return execute(SauceDemoApi.PatchTodo.withPath("id", String.valueOf(id)),
            map().put("completed", completed).build(),
            SauceDemoData.class);
    }

    public Response deleteTodo(int id)
    {
        Log.comment(config, "Deleting todo " + id + " via API");
        return executeRaw(SauceDemoApi.DeleteTodo.withPath("id", String.valueOf(id)), null);
    }

    public SauceDemoData[] listUserTodos(int userId)
    {
        Log.comment(config, "Listing todos for user " + userId + " via API");
        return execute(SauceDemoApi.ListUserTodos.withPath("userId", String.valueOf(userId)), SauceDemoData[].class);
    }

    /**
     * Returns true if every todo in the array has the expected userId.
     * Stream logic kept in the helper so @Test methods stay declarative.
     */
    public boolean allTodosHaveUserId(SauceDemoData[] todos, int userId)
    {
        return Arrays.stream(todos).allMatch(t -> Integer.valueOf(userId).equals(t.getUserId()));
    }
}
