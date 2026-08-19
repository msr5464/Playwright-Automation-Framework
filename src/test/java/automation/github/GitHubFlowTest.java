package automation.github;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.github.GitHubData;
import automation.modules.github.GitHubHelper;
import automation.modules.github.api.GitHubApi;
import automation.modules.github.web.GitHubProfilePage;
import automation.modules.github.web.GitHubRepoPage;
import io.restassured.response.Response;

/**
 * Interleaved GitHub API and web flow tests.
 * Validates that data returned by the GitHub REST API matches what is displayed
 * on the corresponding GitHub web pages.
 */
public class GitHubFlowTest extends TestBase
{

    /**
     * Fetches octocat's public user profile and the Hello-World repository via the GitHub API,
     * then navigates to the corresponding GitHub web pages and cross-verifies the captured
     * API data against the live page content.
     */
    @Test(description = "verify octocat profile and Hello-World repository data across API and Web",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void verifyOctocatProfileAndRepoDataAcrossApiAndWeb(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        // Steps 1–5: Fetch octocat public user profile via the GitHub API
        config.logStep("Fetch octocat public user profile via GitHub API and verify key fields");
        Response userResponse = github.executeRaw(GitHubApi.GetUser.withPath("username", "octocat"), null);
        AssertHelper.assertEquals(config, userResponse.getStatusCode(), 200, "User API should return status 200");
        GitHubData user = userResponse.as(GitHubData.class);
        AssertHelper.assertEquals(config, user.getLogin(), "octocat", "Login field in API response should equal 'octocat'");
        AssertHelper.assertTrue(config, user.getPublicRepos() >= 0, "Public repos count should be >= 0");
        String location = user.getLocation();
        String apiAvatarUrl = userResponse.jsonPath().getString("avatar_url");

        // Steps 6–11: Fetch octocat Hello-World repository via the GitHub API
        config.logStep("Fetch octocat Hello-World repository via GitHub API and verify key fields");
        Response repoResponse = github.executeRaw(
            GitHubApi.GetRepository.withPath("owner", "octocat").withPath("repo", "Hello-World"), null);
        AssertHelper.assertEquals(config, repoResponse.getStatusCode(), 200, "Repository API should return status 200");
        GitHubData repo = repoResponse.as(GitHubData.class);
        String description = repo.getDescription();
        String htmlUrl = repo.getHtmlUrl();
        AssertHelper.assertNotNull(config, description, "Repository description should not be null");
        AssertHelper.assertNotNull(config, htmlUrl, "Repository html_url should not be null");
        AssertHelper.assertTrue(config, htmlUrl.startsWith("https://github.com"),
            "html_url should start with https://github.com");
        AssertHelper.assertTrue(config, repo.getStarsCount() >= 0, "Stargazers count should be >= 0");

        // Steps 12–15: Navigate to octocat's GitHub profile page and cross-verify API data
        config.logStep("Navigate to octocat GitHub profile page and cross-verify location and avatar against API data");
        GitHubProfilePage profilePage = github.navigateToProfile("octocat");
        AssertHelper.assertEquals(config, profilePage.getLocationText(), location,
            "Profile page location should match the location returned by the User API");
        AssertHelper.assertTrue(config, profilePage.isAvatarVisible(),
            "Profile avatar image should be visible on the page");
        String avatarUrlBase = apiAvatarUrl.contains("?")
            ? apiAvatarUrl.substring(0, apiAvatarUrl.indexOf("?"))
            : apiAvatarUrl;
        AssertHelper.assertContains(config, profilePage.getAvatarImageUrl(), avatarUrlBase,
            "Avatar image src on the page should match the avatar_url returned by the User API");

        // Steps 16–18: Navigate to the Hello-World repository page and cross-verify API data
        config.logStep("Navigate to octocat Hello-World repository page and cross-verify description and URL against API data");
        GitHubRepoPage repoPage = github.navigateToRepository(htmlUrl);
        AssertHelper.assertEquals(config, repoPage.getRepositoryDescription(), description,
            "Repository description on the page should match the description returned by the Repository API");
        AssertHelper.assertEquals(config, repoPage.getCurrentUrl(), htmlUrl,
            "Current page URL should match the html_url returned by the Repository API");
    }
}
