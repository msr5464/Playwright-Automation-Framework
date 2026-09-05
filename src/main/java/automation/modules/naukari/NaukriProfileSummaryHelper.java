package automation.modules.naukari;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.WaitHelper;
import automation.core.api.ApiHelper;
import automation.modules.naukari.web.NaukriLoginPage;
import automation.modules.naukari.web.NaukriProfilePage;

/**
 * Helper for Naukri Profile Summary web flows.
 * Orchestrates NaukriLoginPage and NaukriProfilePage for profile summary operations.
 */
public class NaukriProfileSummaryHelper extends ApiHelper
{
    public NaukriProfileSummaryHelper(Config config)
    {
        super(config, config.getRunTimeProperty("naukari.url"));
    }

    /**
     * Toggle the trailing dot in the Naukri Profile Summary.
     * If the current summary text ends with '.', the dot is removed; otherwise a dot is appended.
     * After saving the change, navigates back to the profile page and reads the displayed summary.
     *
     * @param username Naukri account email / username
     * @param password Naukri account password
     * @return String array where index 0 is the modified summary that was saved
     *         and index 1 is the displayed summary read after a page reload
     */
    public String[] toggleProfileSummaryDot(String username, String password)
    {
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("naukari.login.url"));
        NaukriLoginPage loginPage = new NaukriLoginPage(config);
        NaukriProfilePage profilePage = loginPage.doLogin(username, password);

        String currentSummary = profilePage.getProfileSummaryText();
        String modifiedSummary = currentSummary.endsWith(".")
                ? currentSummary.substring(0, currentSummary.length() - 1)
                : currentSummary + ".";

        profilePage.clickEditProfileSummary();
        profilePage.clearAndTypeProfileSummary(modifiedSummary);
        profilePage.saveProfileSummary();
        WaitHelper.waitForNetworkIdle(config);

        String refreshedText = profilePage.refreshAndGetProfileSummaryText();
        return new String[]{modifiedSummary, refreshedText};
    }
}
