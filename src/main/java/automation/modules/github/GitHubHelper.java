package automation.modules.github;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.TestDataReader;
import automation.core.api.ApiHelper;
import automation.core.Enums.ProjectName;
import automation.modules.github.api.GitHubApi;
import automation.modules.github.web.DashboardPage;
import automation.modules.github.web.HomePage;
import automation.modules.github.web.LoginPage;
import automation.modules.github.web.OtpPage;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Unified helper for GitHub web and API flows.
 * Extends ApiHelper so it inherits all execute* methods with the GitHub API base URL.
 *
 * API usage:
 *   GitHubHelper github = new GitHubHelper(config, token);
 *   GitHubData user = github.execute(GitHubApi.GetUser.withPath("username", "octocat"), GitHubData.class);
 *   GitHubData repo = github.execute(GitHubApi.GetRepository.withPath("owner", "torvalds").withPath("repo", "linux"), GitHubData.class);
 *
 * Web usage:
 *   GitHubHelper github = new GitHubHelper(config);
 *   DashboardPage dashboard = github.doLogin(username, password);
 */
public class GitHubHelper extends ApiHelper
{
    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String SESSION_FILE = "GitHubLoginStorage.json";

    public GitHubHelper(Config config)
    {
        super(config, GITHUB_API_BASE);
    }

    public GitHubHelper(Config config, String authToken)
    {
        this(config);
        if (authToken != null) {
            setAuthToken(authToken);
        }
    }

    // ========== WEB FLOWS ==========

    /**
     * Open browser, navigate to GitHub home, and perform a full login.
     */
    public DashboardPage doLogin(String username, String password)
    {
        String githubUrl = config.getRunTimeProperty("githubUrl", "https://github.com/");
        BrowserHelper.navigateTo(config, githubUrl);
        HomePage homePage = new HomePage(config);
        LoginPage loginPage = homePage.clickSignIn();
        return loginPage.doLogin(username, password);
    }

    /**
     * Open browser, navigate to GitHub home, perform login, and handle OTP if required.
     */
    public DashboardPage doLoginWithOtp(String username, String password, String otp)
    {
        String githubUrl = config.getRunTimeProperty("githubUrl", "https://github.com/");
        BrowserHelper.navigateTo(config, githubUrl);
        HomePage homePage = new HomePage(config);
        LoginPage loginPage = homePage.clickSignIn();
        OtpPage otpPage = loginPage.doLoginExpectingOtp(username, password);
        return otpPage.enterOtpAndVerify(otp);
    }

    /**
     * Load GitHub credentials by role for the current environment.
     * CSV: src/test/resources/github/csvFiles/github-users.csv
     * Columns: role, environment, username, password, description
     *
     * Usage:
     *   Map&lt;String, String&gt; credentials = github.getCredentials("admin");
     *   DashboardPage dashboard = github.doLogin(credentials.get("username"), credentials.get("password"));
     */
    public Map<String, String> getCredentials(String role)
    {
        return TestDataReader.loadCsvRowByColumnValue("github", "github-users", "role", role, Config.environment);
    }

    /**
     * Load a previously stored session and navigate to GitHub — no login required.
     */
    public DashboardPage loginWithStoredSession()
    {
        String githubUrl = config.getRunTimeProperty("githubUrl", "https://github.com/");
        Log.step(config, "Loading stored GitHub session from: " + SESSION_FILE);
        BrowserHelper.initBrowserWithStoredSession(config, ProjectName.GitHub, SESSION_FILE);
        BrowserHelper.navigateTo(config, githubUrl);
        return new DashboardPage(config);
    }

    /**
     * Save the current browser session so future tests can skip login.
     */
    public void storeCurrentSession()
    {
        BrowserHelper.storeSession(config, ProjectName.GitHub, SESSION_FILE);
    }

    // ========== API METHODS ==========

    /**
     * Fetch a public GitHub user by username.
     *
     * @param username the GitHub username
     * @return GitHubData populated with the user's public profile fields
     */
    public GitHubData getUser(String username)
    {
        return execute(GitHubApi.GetUser.withPath("username", username), GitHubData.class);
    }

    /**
     * Fetch a public GitHub repository by owner and repo name.
     *
     * @param owner the repository owner's login
     * @param repo  the repository name
     * @return GitHubData populated with the repository's metadata fields
     */
    public GitHubData getRepository(String owner, String repo)
    {
        return execute(GitHubApi.GetRepository.withPath("owner", owner).withPath("repo", repo), GitHubData.class);
    }

    /**
     * Fetch branches for a public GitHub repository.
     *
     * @param owner the repository owner's login
     * @param repo  the repository name
     * @return raw Response containing the JSON array of branch objects
     */
    public Response getRepositoryBranches(String owner, String repo)
    {
        return execute(GitHubApi.GetRepositoryBranches.withPath("owner", owner).withPath("repo", repo));
    }

    /**
     * Fetch contributors for a public GitHub repository.
     *
     * @param owner the repository owner's login
     * @param repo  the repository name
     * @return raw Response containing the JSON array of contributor objects
     */
    public Response getRepositoryContributors(String owner, String repo)
    {
        return execute(GitHubApi.GetRepositoryContributors.withPath("owner", owner).withPath("repo", repo));
    }
}
