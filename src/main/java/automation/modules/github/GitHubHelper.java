package automation.modules.github;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.modules.github.web.DashboardPage;
import automation.modules.github.web.HomePage;
import automation.modules.github.web.LoginPage;
import automation.modules.github.web.OtpPage;

/**
 * Orchestration helper for GitHub web flows.
 */
public class GitHubHelper
{

    private static final String SESSION_FILE = "GitHubLoginStorage.json";

    private final Config config;

    public GitHubHelper(Config config)
    {
        this.config = config;
    }

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
     * Load a previously stored session and navigate to GitHub — no login required.
     */
    public DashboardPage loginWithStoredSession()
    {
        String githubUrl = config.getRunTimeProperty("githubUrl", "https://github.com/");
        Log.step(config, "Loading stored GitHub session from: " + SESSION_FILE);
        BrowserHelper.initBrowserWithStoredSession(config, SESSION_FILE);
        BrowserHelper.navigateTo(config, githubUrl);
        return new DashboardPage(config);
    }

    /**
     * Save the current browser session so future tests can skip login.
     */
    public void storeCurrentSession()
    {
        BrowserHelper.storeSession(config, SESSION_FILE);
    }
}
