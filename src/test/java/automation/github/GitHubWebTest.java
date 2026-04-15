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

/**
 * GitHub web test suite.
 * Tests GitHub UI flows using a real browser via Playwright.
 */
public class GitHubWebTest extends TestBase
{

    /**
     * Log in to GitHub, open the user navigation menu, and verify the
     * displayed name matches the authenticated user.
     */
    @Test(description = "verify displayed name in user menu matches the authenticated user after login",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loginAndValidateUserName(Config config)
    {
        GitHubHelper helper = new GitHubHelper(config);
        Map<String, String> credentials = helper.getCredentials("user");

        config.logStep("Login to GitHub with user credentials");
        DashboardPage dashboardPage = helper.doLogin(credentials.get("username"), credentials.get("password"));

        config.logStep("Open user navigation menu and verify displayed name matches authenticated user");
        UserMenuPage userMenuPage = helper.openUserMenu(dashboardPage);

        String displayedName = userMenuPage.getDisplayedName();
        AssertHelper.assertNotNull(config, displayedName, "Displayed name in user menu should not be null");
        AssertHelper.assertEquals(config, displayedName, credentials.get("name"),
            "Displayed name in user menu should match the authenticated user's name");
    }
}
