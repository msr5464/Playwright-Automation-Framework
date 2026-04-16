package automation.github;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.github.GitHubHelper;
import automation.modules.github.web.DashboardPage;
import automation.modules.github.web.UserMenuPage;

public class GitHubLoginTest extends TestBase
{

    /**
     * Verifies that login is not required when a stored session exists.
     * Run storeFirstTimeLoginOnGitHub first to generate the session file.
     */
    @Test(description="verify if user is able to login to GitHub using Stored Session",dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(testrailData = "1:C1001:WEB", automatedBy = QA.Mukesh)
    public void verifyLoginOnGitHubUsingStoredSession(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);
        DashboardPage dashboard = github.loginWithStoredSession();

        AssertHelper.assertTrue(config, dashboard.isLoggedIn(),
            "User should be logged in to GitHub using stored session");
    }

    /**
     * Performs a full GitHub login and stores the session for future test runs.
     * Credentials are read from config: github.username and github.password
     * (set these in parameters/system.properties which is git-ignored).
     */
    @Test(enabled = false, description="verify if user is able to login to GitHub and also able to Stored Session",dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(testrailData = "1:C1002:WEB", automatedBy = QA.Mukesh)
    public void storeFirstTimeLoginOnGitHub(Config config)
    {
        String username = config.getRunTimeProperty("github.username");
        String password = config.getRunTimeProperty("github.password");

        GitHubHelper github = new GitHubHelper(config);
        DashboardPage dashboard = github.doLogin(username, password);

        AssertHelper.assertTrue(config, dashboard.isLoggedIn(),
            "User should be successfully logged in to GitHub");

        github.storeCurrentSession();
    }

    /**
     * Performs a full GitHub login with OTP/2FA.
     * Credentials and OTP are read from config:
     *   github.username, github.password, github.otp
     */
    @Test(enabled=false, description="verify if user is able to login to GitHub using OTP flow",dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(testrailData = "1:C1003:WEB", automatedBy = QA.Mukesh)
    public void verifyLoginOnGitHubWithOtp(Config config)
    {
        String username = config.getRunTimeProperty("github.username");
        String password = config.getRunTimeProperty("github.password");
        String otp      = "123456"; // In real scenario, this should be generated dynamically or retrieved securely

        GitHubHelper github = new GitHubHelper(config);
        DashboardPage dashboard = github.doLoginWithOtp(username, password, otp);

        AssertHelper.assertTrue(config, dashboard.isLoggedIn(),
            "User should be logged in to GitHub after OTP verification");
    }

    /**
     * Login to GitHub with test credentials, open the user navigation menu,
     * and assert the displayed user name is not blank.
     */
    @Test(description="verify user name is displayed in user menu after login to GitHub", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loginAndValidateUserName(Config config)
    {
        String username = config.getRunTimeProperty("github.username");
        String password = config.getRunTimeProperty("github.password");

        GitHubHelper github = new GitHubHelper(config);

        config.logStep("Login to GitHub and verify user is logged in");
        DashboardPage dashboard = github.doLogin(username, password);
        AssertHelper.assertTrue(config, dashboard.isLoggedIn(),
            "User should be successfully logged in to GitHub");

        config.logStep("Open user navigation menu and verify user name is displayed");
        UserMenuPage userMenu = dashboard.openUserMenu();
        String userName = userMenu.getUserName();
        AssertHelper.assertNotNull(config, userName, "User name should not be null");
        AssertHelper.assertTrue(config, !userName.isEmpty(), "User name should not be empty");
    }
}
