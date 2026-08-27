package automation.naukari;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.naukari.NaukriProfileSummaryHelper;
import automation.modules.naukari.web.NaukriProfilePage;
import org.testng.annotations.Test;

public class NaukriProfileSummaryWebTest extends TestBase
{

    /**
     * Login to Naukri, read the current profile summary, toggle the trailing dot
     * (add if absent, remove if present), save the change, refresh the page,
     * and verify the persisted text matches the expected modified summary.
     */
    @Test(description = "verify profile summary trailing dot toggle persists after save and page refresh",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void toggleDotInProfileSummaryAndVerify(Config config)
    {
        String username = config.getRunTimeProperty("naukari.username");
        String password = config.getRunTimeProperty("naukari.password");

        NaukriProfileSummaryHelper naukri = new NaukriProfileSummaryHelper(config);

        config.logStep("Login to Naukri and navigate to the profile page");
        NaukriProfilePage profilePage = naukri.doLogin(username, password);

        config.logStep("Read the current profile summary and compute the modified text");
        String currentSummary = profilePage.getProfileSummaryText();
        String modifiedSummary = currentSummary.endsWith(".")
            ? currentSummary.substring(0, currentSummary.length() - 1)
            : currentSummary + ".";

        config.logStep("Open the profile summary editor and replace the text with the modified summary");
        profilePage.clickEditProfileSummary();
        profilePage.clearAndTypeProfileSummary(modifiedSummary);

        config.logStep("Save the profile summary and verify the success toast appears");
        profilePage.saveProfileSummary();
        AssertHelper.assertTrue(config, profilePage.isSuccessToastVisible(),
            "Success toast should appear after saving the profile summary");

        config.logStep("Refresh the page and verify the modified profile summary persisted");
        String refreshedSummary = profilePage.refreshAndGetProfileSummaryText();
        AssertHelper.assertEquals(config, refreshedSummary, modifiedSummary,
            "Profile summary after page refresh should match the saved modified summary");
    }

    /**
     * Login to Naukri, use the helper to toggle the trailing dot in the Profile Summary
     * (add if absent, remove if present) and save, then reload the page and assert the
     * modified summary persisted correctly.
     */
    @Test(description = "verify profile summary trailing dot toggle and persistence using helper orchestration",
          dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void toggleDotAndVerifyProfileSummary(Config config)
    {
        String username = config.getRunTimeProperty("naukari.username");
        String password = config.getRunTimeProperty("naukari.password");

        NaukriProfileSummaryHelper naukri = new NaukriProfileSummaryHelper(config);

        config.logStep("Login to Naukri and navigate to the profile page");
        NaukriProfilePage profilePage = naukri.doLogin(username, password);

        config.logStep("Read the current profile summary and compute the modified text");
        String currentSummary = profilePage.getProfileSummaryText();
        String modifiedSummary = currentSummary.endsWith(".")
            ? currentSummary.substring(0, currentSummary.length() - 1)
            : currentSummary + ".";

        config.logStep("Open the profile summary editor, enter the modified text, and save");
        profilePage.clickEditProfileSummary();
        profilePage.clearAndTypeProfileSummary(modifiedSummary);
        profilePage.saveProfileSummary();

        config.logStep("Verify success toast appears confirming the save operation");
        AssertHelper.assertTrue(config, profilePage.isSuccessToastVisible(),
            "Success toast should appear after saving the profile summary");

        config.logStep("Reload the profile page and verify the modified summary persisted");
        String refreshedSummary = profilePage.refreshAndGetProfileSummaryText();
        AssertHelper.assertEquals(config, refreshedSummary, modifiedSummary,
            "Profile summary after page refresh should match the saved modified summary");
    }
}
