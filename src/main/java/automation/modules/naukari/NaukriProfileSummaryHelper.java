package automation.modules.naukari;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.naukari.web.NaukriLoginPage;
import automation.modules.naukari.web.NaukriProfilePage;

/**
 * Helper for Naukri Profile Summary web flows.
 * Extends ApiHelper so it inherits all execute* methods with the Naukri base URL.
 *
 * Web usage:
 *   NaukriProfileSummaryHelper naukri = new NaukriProfileSummaryHelper(config);
 *   NaukriProfilePage profile = naukri.doLogin(username, password);
 *   String modified = naukri.toggleProfileSummaryDot(username, password);
 */
public class NaukriProfileSummaryHelper extends ApiHelper
{
    private final String loginUrl   = config.getRunTimeProperty("naukari.login.url");
    private final String profileUrl = config.getRunTimeProperty("naukari.profile.url");

    public NaukriProfileSummaryHelper(Config config)
    {
        super(config, config.getRunTimeProperty("naukari.url"));
    }

    /**
     * Navigate to the Naukri login page, enter credentials, submit the form,
     * and return a ready-to-use NaukriProfilePage.
     *
     * @param username Naukri account email / username
     * @param password Naukri account password
     * @return NaukriProfilePage once the profile page has loaded
     */
    public NaukriProfilePage doLogin(String username, String password)
    {
        Log.comment(config, "Navigating to Naukri login: " + loginUrl);
        BrowserHelper.navigateTo(config, loginUrl);
        NaukriLoginPage loginPage = new NaukriLoginPage(config);
        return loginPage.doLogin(username, password);
    }

    /**
     * Login to Naukri and toggle the trailing dot in the Profile Summary:
     * if the current summary ends with '.', remove it; otherwise append '.'.
     * Clicks the edit icon, replaces the text, and saves the change.
     *
     * @param username Naukri account email / username
     * @param password Naukri account password
     * @return the modified summary text that was saved, for use in assertions
     */
    public String toggleProfileSummaryDot(String username, String password)
    {
        NaukriProfilePage profilePage = doLogin(username, password);

        String currentSummary  = profilePage.getProfileSummaryText();
        String modifiedSummary = currentSummary.endsWith(".")
            ? currentSummary.substring(0, currentSummary.length() - 1)
            : currentSummary + ".";

        profilePage.clickEditProfileSummary();
        profilePage.clearAndTypeProfileSummary(modifiedSummary);
        profilePage.saveProfileSummary();

        return modifiedSummary;
    }
}
