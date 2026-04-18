package automation.modules.github;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.TestDataReader;
import automation.core.api.ApiHelper;
import automation.core.Enums.ProjectName;
import automation.modules.github.web.DashboardPage;
import automation.modules.github.web.LoginPage;
import automation.modules.github.web.OtpPage;
import automation.modules.github.web.RepoPage;

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
    private static final String GITHUB_LOGIN_URL = "https://github.com/login";
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
     * Open browser, navigate directly to the GitHub login page, and perform a full login.
     * Navigates to /login directly to avoid dependency on the homepage "Sign in" link.
     */
    public DashboardPage doLogin(String username, String password)
    {
        BrowserHelper.navigateTo(config, GITHUB_LOGIN_URL);
        LoginPage loginPage = new LoginPage(config);
        return loginPage.doLogin(username, password);
    }

    /**
     * Open browser, navigate to the GitHub login page, perform login, and handle OTP if required.
     */
    public DashboardPage doLoginWithOtp(String username, String password, String otp)
    {
        BrowserHelper.navigateTo(config, GITHUB_LOGIN_URL);
        LoginPage loginPage = new LoginPage(config);
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

    /**
     * Navigate to a GitHub repository page after authentication.
     * Constructs the repository URL from owner and repo name, navigates the browser,
     * and returns the loaded RepoPage.
     *
     * Usage:
     *   github.doLogin(username, password);
     *   RepoPage repoPage = github.navigateToRepo("automationdemo", "QA-Dashboard");
     */
    public RepoPage navigateToRepo(String owner, String repo)
    {
        String repoUrl = "https://github.com/" + owner + "/" + repo;
        config.logComment("Navigating to repository: " + owner + "/" + repo);
        BrowserHelper.navigateTo(config, repoUrl);
        return new RepoPage(config);
    }
}
