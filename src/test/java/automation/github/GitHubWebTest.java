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

    @Test(description = "Login to GitHub and validate the user name is displayed in the navigation menu",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loginAndValidateUserName(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);
        Map<String, String> credentials = github.getCredentials("admin");

        config.logStep("Login to GitHub with admin credentials");
        DashboardPage dashboard = github.doLogin(credentials.get("username"), credentials.get("password"));

        config.logStep("Verify user is logged in, open user navigation menu, and validate displayed user name");
        AssertHelper.assertTrue(config, dashboard.isLoggedIn(), "User should be logged in after successful login");
        dashboard.openUserMenu();
        String displayName = dashboard.getUserDisplayName();
        AssertHelper.assertNotNull(config, displayName, "User display name should not be null");
        AssertHelper.assertTrue(config, !displayName.isEmpty(), "User display name should not be empty");
    }
}
