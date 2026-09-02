package automation.naukari;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.naukari.NaukriProfileSummaryHelper;
import automation.modules.naukari.web.NaukriProfilePage;
import org.testng.annotations.Test;

public class NaukriProfileSummaryWebTest extends TestBase {
    @Test(description = "verify profile summary dot toggle via helper persists after page reload", dataProvider = "getConfig", groups = {
            GROUP_REGRESSION, GROUP_WEB })
    @TestVariables(automatedBy = QA.Mukesh)
    public void toggleProfileSummaryDotAndVerify(Config config) {
        String username = config.getRunTimeProperty("naukari.username");
        String password = config.getRunTimeProperty("naukari.password");

        NaukriProfileSummaryHelper naukri = new NaukriProfileSummaryHelper(config);

        config.logStep("Login to Naukri, toggle the trailing dot in the profile summary, and save");
        String modifiedSummary = naukri.toggleProfileSummaryDot(username, password);

        config.logStep("Navigate to the profile page and verify the modified summary persisted");
        NaukriProfilePage profilePage = naukri.getProfilePage();
        String actualSummary = profilePage.refreshAndGetProfileSummaryText();
        AssertHelper.assertEquals(config, actualSummary, modifiedSummary,
                "Profile summary after page reload should match the toggled summary");
    }
}
