package automation.modules.github;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.TestDataReader;
import automation.core.api.ApiHelper;
import automation.core.Enums.ProjectName;
import automation.modules.github.web.DashboardPage;
import automation.modules.github.web.HomePage;
import automation.modules.github.web.LoginPage;
import automation.modules.github.web.OtpPage;
import automation.modules.github.web.UserMenuPage;

import java.util.Map;

/**
 * Unified helper for GitHub web and API flows.
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

    public DashboardPage doLogin(String username, String password)
    {
        String githubUrl = config.getRunTimeProperty("githubUrl", "https://github.com/");
        BrowserHelper.navigateTo(config, githubUrl);
        HomePage homePage = new HomePage(config);
        LoginPage loginPage = homePage.clickSignIn();
        return loginPage.doLogin(username, password);
    }

    public DashboardPage doLogin(Map<String, String> credentials)
    {
        return doLogin(credentials.get("username"), credentials.get("password"));
    }

    public DashboardPage doLoginWithOtp(String username, String password, String otp)
    {
        String githubUrl = config.getRunTimeProperty("githubUrl", "https://github.com/");
        BrowserHelper.navigateTo(config, githubUrl);
        HomePage homePage = new HomePage(config);
        LoginPage loginPage = homePage.clickSignIn();
        OtpPage otpPage = loginPage.doLoginExpectingOtp(username, password);
        return otpPage.enterOtpAndVerify(otp);
    }

    public Map<String, String> getCredentials(String role)
    {
        return TestDataReader.loadCsvRowByColumnValue("github", "github-users", "role", role, Config.environment);
    }

    public DashboardPage loginWithStoredSession()
    {
        String githubUrl = config.getRunTimeProperty("githubUrl", "https://github.com/");
        Log.step(config, "Loading stored GitHub session from: " + SESSION_FILE);
        BrowserHelper.initBrowserWithStoredSession(config, ProjectName.GitHub, SESSION_FILE);
        BrowserHelper.navigateTo(config, githubUrl);
        return new DashboardPage(config);
    }

    public void storeCurrentSession()
    {
        BrowserHelper.storeSession(config, ProjectName.GitHub, SESSION_FILE);
    }

    public UserMenuPage openUserMenu(DashboardPage dashboardPage)
    {
        config.logComment("Opening GitHub user navigation menu");
        dashboardPage.openUserMenu();
        return new UserMenuPage(config);
    }
}
