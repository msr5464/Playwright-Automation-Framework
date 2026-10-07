package automation.modules.naukari;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.naukari.web.NaukriLoginPage;
import automation.modules.naukari.web.NaukriProfilePage;

public class NaukriHelper extends ApiHelper
{
    private final String loginUrl;
    private final String profileUrl;

    public NaukriHelper(Config config)
    {
        super(config, config.getRunTimeProperty("naukari.url"));
        this.loginUrl   = config.getRunTimeProperty("naukari.login.url");
        this.profileUrl = config.getRunTimeProperty("naukari.profile.url");
    }

    /**
     * Navigate to the Naukri login page, perform login, then navigate to the profile page.
     *
     * @param email    the login email address
     * @param password the login password
     * @return NaukriProfilePage ready for interaction
     */
    public NaukriProfilePage doLogin(String email, String password)
    {
        Log.comment(config, "Navigating to Naukri login page");
        BrowserHelper.navigateTo(config, loginUrl);
        new NaukriLoginPage(config).doLogin(email, password);
        BrowserHelper.navigateTo(config, profileUrl);
        return new NaukriProfilePage(config);
    }

    /**
     * Navigate directly to the Naukri profile page.
     *
     * @return NaukriProfilePage ready for interaction
     */
    public NaukriProfilePage openProfilePage()
    {
        Log.comment(config, "Navigating to Naukri profile page");
        BrowserHelper.navigateTo(config, profileUrl);
        return new NaukriProfilePage(config);
    }

    /**
     * Toggle the trailing dot of the profile summary and save the change.
     * Adds a dot if the current summary does not end with one; removes it if it does.
     *
     * @param profile        the currently loaded NaukriProfilePage
     * @param currentSummary the current profile summary text
     * @return the expected summary after the toggle (what was saved)
     */
    public String toggleTrailingDotAndSave(NaukriProfilePage profile, String currentSummary)
    {
        String expectedSummary = currentSummary.endsWith(".")
            ? currentSummary.substring(0, currentSummary.length() - 1)
            : currentSummary + ".";
        Log.comment(config, "Toggling trailing dot in profile summary. Saving: " + expectedSummary);
        profile.clickEditSummary();
        profile.setSummaryText(expectedSummary);
        profile.clickSave();
        return expectedSummary;
    }
}
