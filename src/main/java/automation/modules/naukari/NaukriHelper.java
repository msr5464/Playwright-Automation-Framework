package automation.modules.naukari;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.WaitHelper;
import automation.core.api.ApiHelper;
import automation.modules.naukari.web.NaukriLoginPage;
import automation.modules.naukari.web.NaukriProfilePage;

/**
 * Helper for Naukri web flows.
 * Extends ApiHelper so it can be extended with API methods in future if needed.
 *
 * Web usage:
 *   NaukriHelper naukri = new NaukriHelper(config);
 *   NaukriProfilePage profile = naukri.doLogin(username, password);
 */
public class NaukriHelper extends ApiHelper
{
    public NaukriHelper(Config config)
    {
        super(config);
    }

    /**
     * Navigates to the Naukri login page, enters credentials, waits for the post-login
     * redirect to settle, then navigates to the profile page.
     *
     * @param username Naukri account email / username
     * @param password Naukri account password
     * @return NaukriProfilePage — the profile page ready for interaction
     */
    public NaukriProfilePage doLogin(String username, String password)
    {
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("naukari.login.url"));
        NaukriLoginPage loginPage = new NaukriLoginPage(config);
        loginPage.doLogin(username, password);
        WaitHelper.waitForNetworkIdle(config);
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("naukari.profile.url"));
        return new NaukriProfilePage(config);
    }
}
