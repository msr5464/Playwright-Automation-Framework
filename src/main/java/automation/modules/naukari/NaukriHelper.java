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
}
