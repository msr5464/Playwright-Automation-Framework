package automation.github;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.github.GitHubData;
import automation.modules.github.GitHubHelper;
import automation.modules.github.web.RepoPage;

/**
 * Interleaved GitHub flow tests that combine API and web browser steps
 * within a single test method to validate cross-interface consistency.
 */
public class GitHubFlowTest extends TestBase
{

    /**
     * Verify that the repository description shown on the GitHub web page matches
     * the description returned by the GitHub REST API, then validate the owner
     * user profile via API.
     *
     * Steps:
     *   1. API  — GET /repos/octocat/Hello-World and capture description.
     *   2. Web  — Navigate to github.com/octocat/Hello-World.
     *   3. Web  — Verify page description matches the API description from step 1.
     *   4. API  — GET /users/octocat and verify login and public_repos fields.
     */
    @Test(description = "verify repo description on page matches API response and validate owner user profile",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void verifyRepoDescriptionMatchesApiAndValidateUserProfile(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);

        config.logStep("Step 1: Fetch repository metadata via API for octocat/Hello-World");
        GitHubData repoData = github.getRepository("octocat", "Hello-World");
        String apiDescription = repoData.getDescription();

        config.logStep("Step 2: Navigate to the GitHub repository page in the browser");
        RepoPage repoPage = github.navigateToRepo("octocat", "Hello-World");

        config.logStep("Step 3: Verify the description displayed on the page matches the API response");
        String pageDescription = repoPage.getDescription();
        AssertHelper.assertEquals(config, pageDescription, apiDescription,
            "Repository description on the web page should match the API response");

        config.logStep("Step 4: Fetch user profile via API for octocat and verify fields");
        GitHubData user = github.getUser("octocat");
        AssertHelper.assertEquals(config, user.getLogin(), "octocat",
            "User login should be 'octocat'");
        AssertHelper.assertTrue(config, user.getPublicRepos() >= 0,
            "Public repos count should be >= 0");
    }
}
