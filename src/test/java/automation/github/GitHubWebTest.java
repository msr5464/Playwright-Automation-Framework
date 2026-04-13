package automation.github;

import java.util.Map;

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
    @Test(description = "verify user can login to GitHub and the user name is displayed in the user menu",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loginAndValidateUserName(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);
        Map<String, String> credentials = github.getCredentials("admin");

        config.logStep("Login to GitHub with admin credentials");
        DashboardPage dashboard = github.doLogin(credentials);

        config.logStep("Verify user is logged in to GitHub dashboard");
        AssertHelper.assertTrue(config, dashboard.isLoggedIn(), "User should be logged into GitHub");

        config.logStep("Open the user navigation menu and validate the user name");
        dashboard.openUserMenu();
        String actualUserName = dashboard.getUserName();
        AssertHelper.assertNotNull(config, actualUserName, "User name should be displayed in user menu");
        AssertHelper.assertEquals(config, actualUserName, credentials.get("username"),
            "Displayed user name should match the logged-in user");
    }
}
