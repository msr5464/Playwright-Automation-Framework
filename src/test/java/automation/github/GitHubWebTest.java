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

import java.util.Map;

public class GitHubWebTest extends TestBase
{

    @Test(description = "Login to GitHub, open the user navigation menu, and validate the displayed name of the logged-in user",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void loginAndValidateUserName(Config config)
    {
        GitHubHelper github = new GitHubHelper(config);
        Map<String, String> credentials = github.getCredentials("user");

        config.logStep("Login to GitHub with user credentials");
        DashboardPage dashboard = github.doLogin(credentials.get("username"), credentials.get("password"));

        config.logStep("Verify the dashboard page is loaded and user is logged in");
        AssertHelper.assertTrue(config, dashboard.isLoggedIn(), "Dashboard page should confirm user is logged in");

        config.logStep("Open the user navigation menu");
        UserMenuPage userMenu = github.openUserMenu(dashboard);

        config.logStep("Validate the displayed name of the logged-in user");
        String displayedName = userMenu.getDisplayedName();
        AssertHelper.assertNotNull(config, displayedName, "Displayed user name should not be null");
        AssertHelper.assertFalse(config, displayedName.isEmpty(), "Displayed user name should not be empty");
        config.logComment("Validated displayed user name: " + displayedName);
    }
}
