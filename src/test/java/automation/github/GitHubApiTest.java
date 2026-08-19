package automation.github;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.github.GitHubHelper;
import automation.modules.github.GitHubData;
import automation.modules.github.api.GitHubApi;
import io.restassured.response.Response;

import java.util.List;

/**
 * GitHub REST API test suite.
 * Tests public GitHub API endpoints without authentication.
 * Tests authenticated endpoints using a personal access token (set via config).
 */
public class GitHubApiTest extends TestBase
{

    /**
     * Get public user information from GitHub API.
     * This test uses GitHub's public API - no authentication required.
     */
    @Test(description="verify if able to fetch public user information from GitHub API", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C1101:API", automatedBy = QA.Mukesh)
    public void getPublicUserInfo(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        // Fetch a well-known public GitHub account
        GitHubData user = github.execute(GitHubApi.GetUser.withPath("username", "octocat"), GitHubData.class);

        AssertHelper.assertNotNull(config, user.getLogin(), "User login should be returned");
        AssertHelper.assertEquals(config, user.getLogin(), "octocat", "User should be octocat");
        AssertHelper.assertNotNull(config, user.getId(), "User ID should be present");
        AssertHelper.assertTrue(config, user.getPublicRepos() >= 0, "Public repos count should be non-negative");
    }

    /**
     * Get repository information from GitHub.
     * Tests fetching a public repository without authentication.
     */
    @Test(description="verify if able to fetch public repository information from GitHub API", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C1102:API", automatedBy = QA.Mukesh)
    public void getPublicRepository(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        // Fetch a famous public repository
        GitHubData repo = github.execute(GitHubApi.GetRepository.withPath("owner", "torvalds").withPath("repo", "linux"), GitHubData.class);

        AssertHelper.assertNotNull(config, repo.getFullName(), "Repository full name should be present");
        AssertHelper.assertEquals(config, repo.getFullName(), "torvalds/linux", "Repository should be torvalds/linux");
        AssertHelper.assertNotNull(config, repo.getStarsCount(), "Stars count should be present");
        AssertHelper.assertTrue(config, repo.getStarsCount() > 0, "Repository should have stars");
    }

    /**
     * Verify user profile data fields.
     * Tests that all expected fields are present in user response.
     */
    @Test(description="verify user profile contains all required fields from GitHub API", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C1104:API", automatedBy = QA.Mukesh)
    public void verifyUserProfileFields(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        GitHubData user = github.execute(GitHubApi.GetUser.withPath("username", "gvanrossum"), GitHubData.class);

        // Verify essential user fields
        AssertHelper.assertNotNull(config, user.getLogin(), "Login field should be present");
        AssertHelper.assertNotNull(config, user.getId(), "ID field should be present");
        AssertHelper.assertTrue(config, user.getPublicRepos() >= 0, "Public repos should be >= 0");
        AssertHelper.assertTrue(config, user.getFollowers() >= 0, "Followers should be >= 0");
        AssertHelper.assertTrue(config, user.getFollowing() >= 0, "Following should be >= 0");
    }

    /**
     * Verify repository metadata.
     * Tests that repository response includes required metadata fields.
     */
    @Test(description="verify repository contains all required metadata from GitHub API", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C1105:API", automatedBy = QA.Mukesh)
    public void verifyRepositoryMetadata(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        GitHubData repo = github.execute(GitHubApi.GetRepository.withPath("owner", "microsoft").withPath("repo", "vscode"), GitHubData.class);

        // Verify essential repository fields
        AssertHelper.assertNotNull(config, repo.getFullName(), "Full name should be present");
        AssertHelper.assertNotNull(config, repo.getHtmlUrl(), "HTML URL should be present");
        AssertHelper.assertNotNull(config, repo.getStarsCount(), "Stars count should be present");
        AssertHelper.assertNotNull(config, repo.getCreatedAt(), "Created date should be present");
        AssertHelper.assertNotNull(config, repo.getLanguage(), "Language should be present");
    }

    /**
     * Verify repository stars count is accurate.
     * Tests that a famous repository has a reasonable number of stars.
     */
    @Test(description="verify repository has expected engagement metrics from GitHub API", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C1106:API", automatedBy = QA.Mukesh)
    public void verifyRepositoryEngagement(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        // Fetch a popular repository
        GitHubData repo = github.execute(GitHubApi.GetRepository.withPath("owner", "facebook").withPath("repo", "react"), GitHubData.class);

        // React should have more than 100k stars
        AssertHelper.assertTrue(config, repo.getStarsCount() > 100000, 
            "React repository should have > 100k stars");
        AssertHelper.assertNotNull(config, repo.getWatchersCount(), "Watchers count should be present");
    }

    /**
     * Verify user followers and following counts.
     * Tests that user stats are retrievable and reasonable.
     */
    @Test(description="verify user follower and following statistics from GitHub API", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C1107:API", automatedBy = QA.Mukesh)
    public void verifyUserFollowStats(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        // Fetch a prominent user with many followers
        GitHubData user = github.execute(GitHubApi.GetUser.withPath("username", "torvalds"), GitHubData.class);

        AssertHelper.assertNotNull(config, user.getFollowers(), "Followers count should be present");
        AssertHelper.assertNotNull(config, user.getFollowing(), "Following count should be present");
        AssertHelper.assertTrue(config, user.getFollowers() > 0, "User should have followers");
    }

    /**
     * Test invalid username returns 404.
     * Negative test to verify proper error handling.
     */
    @Test(description="verify 404 error when fetching non-existent user from GitHub API", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C1108:API", automatedBy = QA.Mukesh)
    public void verifyNotFoundErrorForInvalidUser(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        var response = github.executeRaw(GitHubApi.GetUser.withPath("username", "this_user_does_not_exist_12345"), null);
        AssertHelper.assertEquals(config, response.getStatusCode(), 404, "Non-existent user should return 404");
    }

    /**
     * Test invalid repository returns 404.
     * Negative test to verify proper error handling for repositories.
     */
    @Test(description="verify 404 error when fetching non-existent repository from GitHub API", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C1109:API", automatedBy = QA.Mukesh)
    public void verifyNotFoundErrorForInvalidRepository(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        var response = github.executeRaw(GitHubApi.GetRepository.withPath("owner", "nonexistentuser1234").withPath("repo", "nonexistentrepo5678"), null);
        AssertHelper.assertEquals(config, response.getStatusCode(), 404, "Non-existent repository should return 404");
    }

    /**
     * Verify octocat's public profile via the GitHub API.
     * Confirms login field equals 'octocat' and public_repos is non-negative.
     */
    @Test(description="verify octocat public profile login and public repos count from GitHub API", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C1110:API", automatedBy = QA.Mukesh)
    public void verifyOctocatPublicProfile(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        GitHubData user = github.getUser("octocat");

        AssertHelper.assertEquals(config, user.getLogin(), "octocat", "Login should be 'octocat'");
        AssertHelper.assertTrue(config, user.getPublicRepos() >= 0, "Public repos count should be non-negative");
    }

    /**
     * Verify the Hello-World repository owned by octocat via the GitHub API.
     * Confirms the repository name is 'Hello-World' and the owner login is 'octocat'.
     */
    @Test(description="verify Hello-World repository name and owner login from GitHub API", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C1111:API", automatedBy = QA.Mukesh)
    public void verifyHelloWorldRepository(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        GitHubData repo = github.getRepository("octocat", "Hello-World");

        AssertHelper.assertEquals(config, repo.getName(), "Hello-World", "Repository name should be 'Hello-World'");
        AssertHelper.assertEquals(config, repo.getOwner().getLogin(), "octocat", "Repository owner login should be 'octocat'");
    }

    /**
     * Verify octocat user profile fields, Hello-World repo metadata, branch list, and contributor list
     * via four sequential GET calls to the GitHub REST API.
     */
    @Test(description = "verify octocat profile, Hello-World repo, branches and contributors via GitHub API",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C1112:API", automatedBy = QA.Mukesh)
    public void verifyOctocatProfileAndRepository(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        config.logStep("Fetch octocat user profile and verify fields");
        Response userResponse = github.executeRaw(GitHubApi.GetUser.withPath("username", "octocat"), null);
        AssertHelper.assertEquals(config, userResponse.getStatusCode(), 200, "GET /users/octocat should return 200");
        GitHubData user = userResponse.as(GitHubData.class);
        AssertHelper.assertEquals(config, user.getLogin(), "octocat", "Login should be 'octocat'");
        AssertHelper.assertTrue(config, user.getPublicRepos() >= 0, "public_repos should be >= 0");
        AssertHelper.assertTrue(config, user.getLocation() != null && !user.getLocation().isEmpty(), "location should not be null or empty");
        String avatarUrl = userResponse.jsonPath().getString("avatar_url");
        AssertHelper.assertTrue(config, avatarUrl != null && avatarUrl.startsWith("http"), "avatar_url should be non-null and start with 'http'");

        config.logStep("Fetch Hello-World repository and verify metadata");
        GitHubData repo = github.getRepository("octocat", "Hello-World");
        AssertHelper.assertEquals(config, repo.getName(), "Hello-World", "Repository name should be 'Hello-World'");
        AssertHelper.assertEquals(config, repo.getOwner().getLogin(), "octocat", "Repository owner login should be 'octocat'");
        AssertHelper.assertTrue(config, repo.getDescription() != null && !repo.getDescription().isEmpty(), "description should not be null or empty");
        AssertHelper.assertTrue(config, repo.getHtmlUrl() != null && repo.getHtmlUrl().contains("github.com"), "html_url should contain 'github.com'");
        AssertHelper.assertTrue(config, repo.getStarsCount() >= 0, "stargazers_count should be >= 0");

        config.logStep("List branches of Hello-World and verify at least one branch exists");
        Response branchResponse = github.listBranches("octocat", "Hello-World");
        List<String> branchNames = branchResponse.jsonPath().getList("name");
        AssertHelper.assertTrue(config, branchNames != null && !branchNames.isEmpty(), "Branch list should contain at least one entry");
        AssertHelper.assertTrue(config, branchNames.contains("master") || branchNames.contains("main"), "Branch list should contain 'master' or 'main'");

        config.logStep("List contributors of Hello-World and verify 'octocat' is a contributor");
        Response contributorResponse = github.listContributors("octocat", "Hello-World");
        List<String> contributorLogins = contributorResponse.jsonPath().getList("login");
        AssertHelper.assertTrue(config, contributorLogins != null && !contributorLogins.isEmpty(), "Contributor list should contain at least one entry");
        AssertHelper.assertTrue(config, contributorLogins.contains("octocat"), "Contributor list should contain 'octocat'");
    }
}
