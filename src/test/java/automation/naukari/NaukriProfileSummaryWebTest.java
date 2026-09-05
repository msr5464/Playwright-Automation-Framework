package automation.naukari;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.naukari.NaukriProfileSummaryHelper;
import org.testng.annotations.Test;

public class NaukriProfileSummaryWebTest extends TestBase
{

    /**
     * Toggle the trailing dot in the Naukri Profile Summary, save the change, navigate
     * back to the profile page, and verify the updated summary is reflected correctly.
     * If the current summary ends with '.' the dot is removed; otherwise a dot is appended.
     */
    @Test(description = "Toggle trailing dot in Profile Summary, save, and verify the change is persisted on the profile page",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void toggleProfileSummaryDotAndVerify(Config config)
    {
        String username = config.getRunTimeProperty("naukari.username");
        String password = config.getRunTimeProperty("naukari.password");

        NaukriProfileSummaryHelper helper = new NaukriProfileSummaryHelper(config);

        config.logStep("Login to Naukri, toggle the trailing dot in Profile Summary, save the change, and verify it persists after page reload");
        String[] result = helper.toggleProfileSummaryDot(username, password);
        String modifiedSummary  = result[0];
        String displayedSummary = result[1];

        AssertHelper.assertEquals(config, displayedSummary, modifiedSummary,
            "Profile Summary displayed after page reload should match the saved modified summary");
    }
}
