package automation.github;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.github.GitHubHelper;
import automation.modules.github.web.DashboardPage;
import automation.modules.github.web.RepoPage;

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
     * Validates that the Test Coverage image is visible on the QA-Dashboard repository page.
     * Logs in to GitHub, navigates to the automationdemo/QA-Dashboard repository,
     * scrolls to the 'Test Coverage Data for all the Projects:' section, and asserts
     * the associated coverage image is visible.
     */
    @Test(description = "validate test coverage image is visible on the QA-Dashboard repository page", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(testrailData = "1:C1004:WEB", automatedBy = QA.Mukesh)
    public void validateTestCoverageImageOnQADashboard(Config config)
    {
        String username = config.getRunTimeProperty("github.username");
        String password = config.getRunTimeProperty("github.password");

        GitHubHelper github = new GitHubHelper(config);

        config.logStep("Login to GitHub with credentials");
        github.doLogin(username, password);

        config.logStep("Navigate to automationdemo/QA-Dashboard repository");
        RepoPage repoPage = github.navigateToRepo("automationdemo", "QA-Dashboard");

        config.logStep("Scroll to Test Coverage section and validate the coverage image is visible");
        repoPage.scrollToTestCoverageSection();

        AssertHelper.assertTrue(config, repoPage.isTestCoverageImageVisible(),
            "Test Coverage image should be visible on the QA-Dashboard repository page");
    }
}
