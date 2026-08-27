package automation.demo;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.demo.DemoHelper;
import automation.modules.demo.web.DemoHomePage;

public class DemoWebTest extends TestBase
{
    @Test(description = "verify home page title contains Example", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void verifyHomePageTitle(Config config)
    {
        DemoHelper demo = new DemoHelper(config);

        config.logStep("Visit Demo home page and verify title contains 'Example'");
        DemoHomePage homePage = demo.visitHomePage();

        AssertHelper.assertTrue(config, homePage.isTitleContainingExample(),
            "Home page title should contain 'Example'");
    }
}
