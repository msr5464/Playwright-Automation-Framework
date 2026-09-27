package automation.naukari;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.naukari.NaukriHelper;
import automation.modules.naukari.web.NaukriProfilePage;
import org.testng.annotations.Test;

public class NaukriProfileSummaryWebTest extends TestBase
{
    /**
     * Toggle trailing dot in profile summary and verify the change persisted after reload.
     */
    @Test(description = "Toggle trailing dot in profile summary and verify the change persisted after reload",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void toggleProfileSummaryDot(Config config)
    {
        String username = config.getRunTimeProperty("naukari.username");
        String password = config.getRunTimeProperty("naukari.password");
        NaukriHelper naukri = new NaukriHelper(config);

        config.logStep("Login to Naukri and open the profile page");
        NaukriProfilePage profile = naukri.doLogin(username, password);

        config.logStep("Read the current profile summary text");
        String currentSummary = profile.getCurrentSummaryText();

        config.logStep("Determine the expected summary by toggling the trailing dot");
        String expectedSummary = currentSummary.endsWith(".")
            ? currentSummary.substring(0, currentSummary.length() - 1)
            : currentSummary + ".";

        config.logStep("Click the edit button for the Profile Summary section");
        profile.clickEditSummary();

        config.logStep("Enter the updated summary text in the text area");
        profile.setSummaryText(expectedSummary);

        config.logStep("Save the updated profile summary");
        profile.clickSave();

        config.logStep("Navigate back to the profile page to verify the change persisted");
        profile = naukri.openProfilePage();

        config.logStep("Verify the displayed profile summary matches the saved value");
        AssertHelper.assertEquals(config, profile.getDisplayedSummaryText(), expectedSummary,
            "Profile Summary after reload should match the saved modified summary");
    }
}
