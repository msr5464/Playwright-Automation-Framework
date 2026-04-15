package automation.github;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.github.GitHubHelper;
import automation.modules.github.web.DashboardPage;

public class GitHubWebTest extends TestBase
{

    /**
     * Login to GitHub with demo credentials, open the user navigation menu,
     * and validate that the displayed user name is not blank.
     */
    @Test(description = "Login to GitHub, open user menu, and validate the displayed user name is not blank",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loginAndValidateUserName(Config config)
    {
        String username = "automationdemo@yopmail.com";
        String password = "automationPassword";

        GitHubHelper github = new GitHubHelper(config);

        config.logStep("Login to GitHub with demo credentials");
        DashboardPage dashboardPage = github.doLogin(username, password);

        config.logStep("Verify user is logged in by checking avatar visibility");
        AssertHelper.assertTrue(config, dashboardPage.isLoggedIn(), "User should be logged in after successful login");

        config.logStep("Open user navigation menu and validate user name is not blank");
        dashboardPage.openUserMenu();
        String userName = dashboardPage.getUserNameFromMenu();
        AssertHelper.assertNotNull(config, userName, "User name from menu should not be null");
        AssertHelper.assertTrue(config, !userName.isBlank(), "User name from menu should not be blank");
    }
}
