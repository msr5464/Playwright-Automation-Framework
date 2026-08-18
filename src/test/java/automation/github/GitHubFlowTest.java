package automation.github;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.github.GitHubHelper;
import automation.modules.github.GitHubData;
import automation.modules.github.api.GitHubApi;
import automation.modules.github.web.ProfilePage;

import java.util.List;

/**
 * Interleaved API + Web flow tests for GitHub.
 * Validates that data returned by the API is correctly reflected in the browser UI.
 */
public class GitHubFlowTest extends TestBase
{

    /**
     * Interleaved flow: fetch octocat's profile from the API, open the browser profile page,
     * verify the bio and avatar on the UI match the API data, then capture repo topics via API.
     *
     * Steps:
     *   1. API  — GET /users/octocat — capture bio and avatar_url
     *   2. WEB  — Navigate to github.com/octocat profile page
     *   3. WEB  — Verify bio displayed on the page matches the API bio
     *   4. WEB  — Verify profile avatar image is visible
     *   5. API  — GET /repos/octocat/Hello-World — capture topics list
     */
    @Test(description = "verify API profile data matches web UI then capture repo topics for octocat",
          dataProvider = "getConfig",
          groups = {GROUP_REGRESSION, GROUP_API, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void verifyProfileBioAndAvatarMatchApiThenCaptureRepoTopics(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        config.logStep("Step 1: Fetch octocat user profile from GitHub API");
        GitHubData apiUser = github.execute(GitHubApi.GetUser.withPath("username", "octocat"), GitHubData.class);
        String apiBio = apiUser.getBio();
        AssertHelper.assertNotNull(config, apiUser.getLogin(), "API response should contain login field");
        AssertHelper.assertNotNull(config, apiUser.getAvatarUrl(), "API response should contain avatar_url field");

        config.logStep("Step 2: Navigate to octocat GitHub profile page in browser");
        ProfilePage profilePage = github.navigateToProfile("octocat");

        config.logStep("Step 3: Verify bio on page matches bio from API");
        if (apiBio != null && !apiBio.isEmpty()) {
            String pageBio = profilePage.getBioText();
            AssertHelper.assertContains(config, pageBio, apiBio, "Profile page bio should match bio returned by API");
        } else {
            config.logComment("API returned no bio for octocat — bio display check skipped");
        }

        config.logStep("Step 4: Verify profile avatar image is visible on the page");
        AssertHelper.assertTrue(config, profilePage.isAvatarVisible(),
            "Profile avatar image should be visible on the page");

        config.logStep("Step 5: Fetch Hello-World repository from GitHub API and capture topics");
        Response repoResponse = github.executeRaw(
            GitHubApi.GetRepository.withPath("owner", "octocat").withPath("repo", "Hello-World"), null);
        AssertHelper.assertEquals(config, repoResponse.getStatusCode(), 200,
            "GET /repos/octocat/Hello-World should return 200");
        List<String> topics = repoResponse.jsonPath().getList("topics", String.class);
        config.logComment("Captured repository topics: " + topics);
    }
}
