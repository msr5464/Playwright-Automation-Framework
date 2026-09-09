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
     * Toggles the trailing dot in the Profile Summary, saves the change, navigates
     * back to the profile page, and verifies the updated summary is reflected.
     */
    @Test(description = "Toggle the trailing dot in the Profile Summary, save the change, then navigate back to the profile page and verify the updated summary is reflected",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void toggleProfileSummaryDotAndVerify(Config config)
    {
        String username = config.getRunTimeProperty("naukari.username");
        String password = config.getRunTimeProperty("naukari.password");
        NaukriHelper naukri = new NaukriHelper(config);

        config.logStep("Login to Naukri with stored credentials and open the profile page");
        NaukriProfilePage profile = naukri.doLogin(username, password);

        config.logStep("Read the current Profile Summary text");
        String currentSummary = profile.getProfileSummaryText();

        String modifiedSummary = currentSummary.endsWith(".")
            ? currentSummary.substring(0, currentSummary.length() - 1)
            : currentSummary + ".";

        config.logStep("Click the edit icon next to Profile Summary and wait for the text area to appear");
        profile.clickEditProfileSummary();

        config.logStep("Clear the Profile Summary text area and type the modified summary");
        profile.clearAndTypeProfileSummary(modifiedSummary);

        config.logStep("Click Save to persist the modified Profile Summary");
        profile.saveProfileSummary();

        config.logStep("Navigate back to the profile page and read the displayed Profile Summary");
        String displayedSummary = profile.refreshAndGetProfileSummaryText();

        config.logStep("Verify the displayed Profile Summary matches the modified summary");
        AssertHelper.assertEquals(config, displayedSummary, modifiedSummary,
            "Profile Summary after reload should match the saved modified summary");
    }
}
