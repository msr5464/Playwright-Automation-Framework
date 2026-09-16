package automation.saucedemo;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.saucedemo.SauceDemoData;
import automation.modules.saucedemo.SauceDemoHelper;
import io.restassured.response.Response;

public class SauceDemoApiTest extends TestBase
{

    @Test(description = "Verify a complete lifecycle of a post including fetch all, create, read, update, and delete", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void simulateApiLifecycle(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);
        java.util.Map<String, String> testData = api.getPostData("lifecycle");
        int limit = Integer.parseInt(testData.get("limit"));
        int userId = Integer.parseInt(testData.get("userId"));
        int postId = Integer.parseInt(testData.get("postId"));
        String title = testData.get("title");
        String body = testData.get("body");

        config.logStep("Step 1: Retrieve posts (with limit query param) and verify the list is not empty");
        SauceDemoData[] allPosts = api.getAllPosts(limit);
        AssertHelper.assertTrue(config, allPosts != null && allPosts.length > 0, "Posts list should not be empty");
        AssertHelper.assertTrue(config, allPosts.length <= limit, "Posts list should respect the _limit query parameter");
        AssertHelper.assertNotNull(config, allPosts[0].getId(), "First post should have a valid ID");

        config.logStep("Step 2: Create a new post for a specific user (demonstrates custom headers / Auth)");
        SauceDemoData createdPost = api.createPost(userId, title, body);
        AssertHelper.assertNotNull(config, createdPost.getId(), "Created post should have a generated ID");
        AssertHelper.assertEquals(config, createdPost.getTitle(), title, "Created title should match");
        AssertHelper.assertEquals(config, createdPost.getUserId(), userId, "Created user ID should match");

        config.logStep("Step 3: Fetch an existing post to prepare for update (JSONPlaceholder mocks persistence)");
        SauceDemoData existingPost = api.getPost(postId);
        AssertHelper.assertEquals(config, existingPost.getId(), postId, "Fetched post ID should be " + postId);

        config.logStep("Step 4: Update the fetched post with modified content");
        String updatedTitle = existingPost.getTitle() + " - Updated";
        String updatedBody = "The body has been updated during the lifecycle test.";
        SauceDemoData updatedPost = api.updatePost(postId, existingPost.getUserId(), updatedTitle, updatedBody);
        AssertHelper.assertEquals(config, updatedPost.getTitle(), updatedTitle, "Post title should be updated");
        AssertHelper.assertEquals(config, updatedPost.getBody(), updatedBody, "Post body should be updated");

        config.logStep("Step 5: Delete the post and verify the operation succeeds");
        Response deleteResponse = api.deletePost(postId);
        AssertHelper.assertEquals(config, deleteResponse.getStatusCode(), 200, "Delete operation should return 200 OK");
        
        config.logStep("Step 6: Verify fetching a non-existent post correctly returns 404");
        Response notFoundResponse = api.getPostRaw(999999);
        AssertHelper.assertEquals(config, notFoundResponse.getStatusCode(), 404, "Fetching invalid post should return 404");
    }

    @Test(description = "Verify a post can be fetched by ID", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void getPostById(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);
        java.util.Map<String, String> testData = api.getPostData("get_post");
        int postId = Integer.parseInt(testData.get("postId"));

        config.logStep("Fetch post with ID " + postId + " and verify response fields");
        SauceDemoData post = api.getPost(postId);

        AssertHelper.assertNotNull(config, post.getId(), "Post ID should be present");
        AssertHelper.assertEquals(config, post.getId(), postId, "Post ID should be " + postId);
        AssertHelper.assertNotNull(config, post.getTitle(), "Post title should be present");
        AssertHelper.assertNotNull(config, post.getBody(), "Post body should be present");
    }

    @Test(description = "Verify a new post can be created", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void createPost(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);
        java.util.Map<String, String> testData = api.getPostData("create_post");
        int userId = Integer.parseInt(testData.get("userId"));
        String title = testData.get("title");
        String body = testData.get("body");

        config.logStep("Create a new post and verify it is returned in the response");
        SauceDemoData created = api.createPost(userId, title, body);

        AssertHelper.assertNotNull(config, created.getId(), "Created post should have an ID");
        AssertHelper.assertEquals(config, created.getTitle(), title, "Title should match");
        AssertHelper.assertEquals(config, created.getBody(), body, "Body should match");
    }

    @Test(description = "Verify an existing post can be updated", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void updatePost(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);
        java.util.Map<String, String> testData = api.getPostData("update_post");
        int postId = Integer.parseInt(testData.get("postId"));
        int userId = Integer.parseInt(testData.get("userId"));
        String title = testData.get("title");
        String body = testData.get("body");

        config.logStep("Update post " + postId + " with a new title and body, and verify the response");
        SauceDemoData updated = api.updatePost(postId, userId, title, body);

        AssertHelper.assertEquals(config, updated.getTitle(), title, "Updated title should match");
        AssertHelper.assertEquals(config, updated.getBody(), body, "Updated body should match");
    }

    @Test(description = "Verify fetching a non-existent post returns 404", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void getNonExistentPost_returns404(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);
        java.util.Map<String, String> testData = api.getPostData("not_found");
        int postId = Integer.parseInt(testData.get("postId"));

        config.logStep("Fetch a post ID that does not exist (" + postId + ") and verify 404 is returned");
        Response response = api.getPostRaw(postId);

        AssertHelper.assertEquals(config, response.getStatusCode(), 404, "Non-existent post should return 404");
    }

    @Test(description = "Create a todo via POST and verify the 201 response fields", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void createTodo(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);
        java.util.Map<String, String> data = api.getTodoData("create_todo");
        int userId = Integer.parseInt(data.get("userId"));
        String title = data.get("title");

        config.logStep("Call CreateTodo with userId=" + userId + " and title='" + title + "' and verify the response returns status 201");
        SauceDemoData created = api.createTodo(userId, title, false);

        config.logStep("Verify the response contains a generated ID");
        AssertHelper.assertNotNull(config, created.getId(), "Created todo should have a generated ID");

        config.logStep("Verify created userId equals " + userId);
        AssertHelper.assertEquals(config, created.getUserId(), userId, "Created userId should match request");

        config.logStep("Verify created title equals '" + title + "'");
        AssertHelper.assertEquals(config, created.getTitle(), title, "Created title should match request");

        config.logStep("Verify completed flag is false on newly created todo");
        AssertHelper.assertFalse(config, created.getCompleted(), "Completed should be false on newly created todo");
    }

    @Test(description = "Fetch todo 1 via GET and verify id and userId", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void fetchTodo(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);
        java.util.Map<String, String> data = api.getTodoData("fetch_todo");
        int todoId = Integer.parseInt(data.get("todoId"));
        int userId = Integer.parseInt(data.get("userId"));

        config.logStep("Call GetTodo with id=" + todoId + " and verify the response returns status 200");
        SauceDemoData todo = api.getTodo(todoId);

        config.logStep("Verify returned todo id equals " + todoId);
        AssertHelper.assertEquals(config, todo.getId(), todoId, "Todo id should be " + todoId);

        config.logStep("Verify returned todo userId equals " + userId);
        AssertHelper.assertEquals(config, todo.getUserId(), userId, "Todo userId should be " + userId);
    }

    @Test(description = "Replace todo 1 via PUT and verify the updated title", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void replaceTodo(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);
        java.util.Map<String, String> data = api.getTodoData("replace_todo");
        int todoId = Integer.parseInt(data.get("todoId"));
        int userId = Integer.parseInt(data.get("userId"));
        String title = data.get("title");

        config.logStep("Call UpdateTodo with id=" + todoId + " and title='" + title + "' and verify the response returns status 200");
        SauceDemoData replaced = api.updateTodo(todoId, userId, title, false);

        config.logStep("Verify the replaced todo title equals '" + title + "'");
        AssertHelper.assertEquals(config, replaced.getTitle(), title, "Replaced title should match request");
    }

    @Test(description = "Patch todo 1 to mark it completed and verify completed flag and non-empty title", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void patchTodo(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);
        java.util.Map<String, String> data = api.getTodoData("patch_todo");
        int todoId = Integer.parseInt(data.get("todoId"));

        config.logStep("Call PatchTodo with id=" + todoId + " to set completed=true and verify the response returns status 200");
        SauceDemoData patched = api.patchTodo(todoId, true);

        config.logStep("Verify completed flag is true after patching todo " + todoId);
        AssertHelper.assertTrue(config, patched.getCompleted(), "Completed flag should be true after patch");

        config.logStep("Verify the patched todo title is present and non-empty");
        AssertHelper.assertNotNull(config, patched.getTitle(), "Title should be present after patch");
        AssertHelper.assertTrue(config, patched.getTitle() != null && !patched.getTitle().isEmpty(), "Title should not be empty after patch");
    }

    @Test(description = "Delete todo 1 via DELETE and verify 200 status", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void deleteTodo(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);
        java.util.Map<String, String> data = api.getTodoData("delete_todo");
        int todoId = Integer.parseInt(data.get("todoId"));

        config.logStep("Call DeleteTodo with id=" + todoId + " and verify the operation returns status 200");
        api.deleteTodo(todoId);
    }

    @Test(description = "List todos for user 1 and verify count and userId on every entry", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void listUserTodos(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);
        java.util.Map<String, String> data = api.getTodoData("list_todos");
        int userId = Integer.parseInt(data.get("userId"));

        config.logStep("Call ListUserTodos with userId=" + userId + " and verify the response returns status 200");
        java.util.List<SauceDemoData> todos = api.listUserTodos(userId);

        config.logStep("Verify the returned list contains exactly 20 todos for user " + userId);
        AssertHelper.assertEquals(config, todos.size(), 20, "User " + userId + " should have exactly 20 todos");

        config.logStep("Verify every todo in the list has userId=" + userId);
        AssertHelper.assertTrue(config, api.allTodosHaveUserId(todos, userId), "All todos in the list should have userId " + userId);
    }
}
