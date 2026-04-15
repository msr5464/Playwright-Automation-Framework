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

    @Test(description = "Login to GitHub, open user navigation menu, and validate the displayed user name",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loginAndValidateUserName(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);
        String username = config.getRunTimeProperty("github.username");
        String password = config.getRunTimeProperty("github.password");

        config.logStep("Login to GitHub with provided credentials");
        DashboardPage dashboard = github.doLogin(username, password);

        config.logStep("Verify user is logged in successfully");
        AssertHelper.assertTrue(config, dashboard.isLoggedIn(), "User should be logged in after successful login");

        config.logStep("Open user navigation menu and retrieve the displayed user name");
        dashboard.openUserMenu();
        String userName = dashboard.getUserNameFromMenu();

        config.logStep("Verify user name is present in the navigation menu");
        AssertHelper.assertNotNull(config, userName, "User name should be displayed in the navigation menu");
    }
}
