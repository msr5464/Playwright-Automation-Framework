package automation.saucedemo;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.saucedemo.PostBuilder;
import automation.modules.saucedemo.PostData;
import automation.modules.saucedemo.SauceDemoHelper;
import automation.modules.saucedemo.api.PostApi;
import io.restassured.response.Response;

public class SauceDemoApiTest extends TestBase
{

    @Test(description = "verify a post can be fetched by ID", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void getPostById(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);

        config.logStep("Fetch post with ID 1 and verify response fields");
        PostData post = api.execute(PostApi.GetPost.withPath("id", "1"), PostData.class);

        AssertHelper.assertNotNull(config, post.getId(), "Post ID should be present");
        AssertHelper.assertEquals(config, post.getId(), 1, "Post ID should be 1");
        AssertHelper.assertNotNull(config, post.getTitle(), "Post title should be present");
        AssertHelper.assertNotNull(config, post.getBody(), "Post body should be present");
    }

    @Test(description = "verify a new post can be created", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void createPost(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);

        config.logStep("Create a new post and verify it is returned in the response");
        PostData request = new PostBuilder().withTitle("My Test Post").withBody("Some content").build();
        PostData created = api.execute(PostApi.CreatePost, request, PostData.class);

        AssertHelper.assertNotNull(config, created.getId(), "Created post should have an ID");
        AssertHelper.assertEquals(config, created.getTitle(), request.getTitle(), "Title should match");
        AssertHelper.assertEquals(config, created.getBody(), request.getBody(), "Body should match");
    }

    @Test(description = "verify an existing post can be updated", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void updatePost(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);

        config.logStep("Update post 1 with a new title and body, and verify the response");
        PostData updateRequest = new PostBuilder().withUserId(1).withTitle("Updated Title").withBody("Updated body").build();
        PostData updated = api.execute(PostApi.UpdatePost.withPath("id", "1"), updateRequest, PostData.class);

        AssertHelper.assertEquals(config, updated.getTitle(), "Updated Title", "Updated title should match");
        AssertHelper.assertEquals(config, updated.getBody(), "Updated body", "Updated body should match");
    }

    @Test(description = "verify a post can be deleted", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void deletePost(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);

        config.logStep("Delete post 1 and verify API returns 200");
        api.execute(PostApi.DeletePost.withPath("id", "1"));
    }

    @Test(description = "verify fetching a non-existent post returns 404", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void getNonExistentPost_returns404(Config config)
    {
        SauceDemoHelper api = new SauceDemoHelper(config);

        config.logStep("Fetch a post ID that does not exist and verify 404 is returned");
        Response response = api.executeRaw(PostApi.GetPost.withPath("id", "99999"), null);

        AssertHelper.assertEquals(config, response.getStatusCode(), 404, "Non-existent post should return 404");
    }
}
