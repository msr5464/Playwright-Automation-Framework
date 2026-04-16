package automation.github;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.github.GitHubHelper;
import automation.modules.github.web.DashboardPage;

import java.util.Map;

public class GitHubWebTest extends TestBase
{

    @Test(description = "Login to GitHub, open user navigation menu, and validate the displayed user name",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loginAndVerifyUserName(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);
        Map<String, String> credentials = github.getCredentials("admin");

        config.logStep("Login to GitHub using admin credentials");
        DashboardPage dashboard = github.doLogin(credentials.get("username"), credentials.get("password"));

        config.logStep("Verify user is logged in on the dashboard");
        AssertHelper.assertTrue(config, dashboard.isLoggedIn(), "User account menu should be visible after login");

        config.logStep("Open the user navigation menu and verify the user name is displayed");
        dashboard.openUserMenu();
        String userName = dashboard.getUserNameFromMenu();
        AssertHelper.assertNotNull(config, userName, "User name in navigation menu should not be null");
        AssertHelper.assertTrue(config, !userName.isEmpty(), "User name in navigation menu should not be empty");
    }
}
