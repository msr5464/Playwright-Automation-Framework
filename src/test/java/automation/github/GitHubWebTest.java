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

    @Test(description = "Login to GitHub, click the user navigation menu, and validate the displayed user name",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loginAndVerifyUserName(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);
        Map<String, String> credentials = github.getCredentials("admin");

        config.logStep("Login to GitHub with standard user credentials");
        DashboardPage dashboard = github.doLogin(credentials.get("username"), credentials.get("password"));

        config.logStep("Open user navigation menu and verify user name is displayed");
        dashboard.openUserMenu();
        String userName = dashboard.getUserNameFromMenu();

        AssertHelper.assertNotNull(config, userName, "User name should be displayed in the navigation menu");
        AssertHelper.assertTrue(config, !userName.isEmpty(), "User name should not be empty");
    }
}
