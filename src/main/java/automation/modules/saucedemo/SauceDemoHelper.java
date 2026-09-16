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
import java.util.List;
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

    /**
     * Load todo test data by todo_key from todos.csv.
     * CSV: src/test/resources/saucedemo/csvFiles/todos.csv
     */
    public Map<String, String> getTodoData(String todoKey)
    {
        return TestDataReader.loadCsvRowByColumnValue(
            "saucedemo", "todos", "todo_key", todoKey, Config.environment);
    }

    /**
     * Create a todo via POST /todos.
     */
    public SauceDemoData createTodo(int userId, String title, boolean completed)
    {
        Log.comment(config, "Creating todo via API - userId: " + userId + ", title: " + title);
        SauceDemoData request = new SauceDemoData();
        request.setUserId(userId);
        request.setTitle(title);
        request.setCompleted(completed);
        return execute(SauceDemoApi.CreateTodo, request, SauceDemoData.class);
    }

    /**
     * Fetch a single todo by ID via GET /todos/{id}.
     */
    public SauceDemoData getTodo(int todoId)
    {
        Log.comment(config, "Fetching todo " + todoId + " via API");
        return execute(SauceDemoApi.GetTodo.withPath("id", String.valueOf(todoId)), SauceDemoData.class);
    }

    /**
     * Replace a todo via PUT /todos/{id}.
     */
    public SauceDemoData updateTodo(int todoId, int userId, String title, boolean completed)
    {
        Log.comment(config, "Replacing todo " + todoId + " via API");
        SauceDemoData request = new SauceDemoData();
        request.setId(todoId);
        request.setUserId(userId);
        request.setTitle(title);
        request.setCompleted(completed);
        return execute(SauceDemoApi.UpdateTodo.withPath("id", String.valueOf(todoId)), request, SauceDemoData.class);
    }

    /**
     * Patch a todo's completed field via PATCH /todos/{id}.
     */
    public SauceDemoData patchTodo(int todoId, boolean completed)
    {
        Log.comment(config, "Patching todo " + todoId + " with completed=" + completed);
        SauceDemoData patchBody = new SauceDemoData();
        patchBody.setCompleted(completed);
        return execute(SauceDemoApi.PatchTodo.withPath("id", String.valueOf(todoId)), patchBody, SauceDemoData.class);
    }

    /**
     * Delete a todo via DELETE /todos/{id}. Asserts 200 internally.
     */
    public void deleteTodo(int todoId)
    {
        Log.comment(config, "Deleting todo " + todoId + " via API");
        execute(SauceDemoApi.DeleteTodo.withPath("id", String.valueOf(todoId)));
    }

    /**
     * List all todos for a user via GET /users/{userId}/todos.
     */
    public List<SauceDemoData> listUserTodos(int userId)
    {
        Log.comment(config, "Listing todos for user " + userId + " via API");
        SauceDemoData[] todos = execute(
            SauceDemoApi.ListUserTodos.withPath("userId", String.valueOf(userId)),
            SauceDemoData[].class);
        return Arrays.asList(todos);
    }

    /**
     * Returns true if every todo in the list has the given userId.
     */
    public boolean allTodosHaveUserId(List<SauceDemoData> todos, int userId)
    {
        return todos.stream().allMatch(t -> t.getUserId() != null && t.getUserId().equals(userId));
    }
}
