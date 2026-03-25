package automation.modules.access;

import io.restassured.response.Response;

import automation.core.api.ApiHelper;
import automation.core.BrowserHelper;
import automation.core.User;
import automation.core.Config;
import automation.core.Log;
import automation.modules.access.web.DashboardPage;
import automation.modules.access.web.LoginPage;

import java.util.Map;

/**
 * Authentication helper — extends ApiHelper so it can perform both
 * API auth flows and web login from a single instance.
 *
 * Usage (API test):
 *   AuthHelper auth = new AuthHelper(config);
 *   auth.loginAndSetAuth(user);
 *   auth.execute(CardApi.CreateCard, card, CardData.class);
 *
 * Usage (web test):
 *   AuthHelper auth = new AuthHelper(config);
 *   DashboardPage dashboard = auth.doLogin(user);
 */
public class AuthHelper extends ApiHelper
{

    public AuthHelper(Config config)
    {
        super(config);
    }

    // ========== WEB LOGIN ==========

    /**
     * Navigate to the portal, log in via UI, and return the DashboardPage.
     * Assumes browser is already initialized via BrowserHelper.initBrowser(config).
     *
     * Usage:
     *   DashboardPage dashboard = auth.doLogin(user);
     *   CardListPage cardList   = dashboard.navigateToCards();
     *   CardPage cardPage       = cardList.clickCreateCard();
     */
    public DashboardPage doLogin(User user)
    {
        String portalUrl = config.getRunTimeProperty("customerPortalUrl");
        BrowserHelper.navigateTo(config, portalUrl);
        LoginPage loginPage = new LoginPage(config);
        loginPage.login(user);
        return new DashboardPage(config);
    }

    // ========== API LOGIN ==========

    /**
     * Full API login flow: email + password + OTP → sets shared auth for all subsequent API calls.
     */
    public void loginAndSetAuth(User user)
    {
        Log.step(config, "Authenticating: " + user.getUsername());

        Response loginResponse = post("/v1/auth/login",
            Map.of("email", user.getUsername(), "password", user.getPassword()));
        assertSuccess(loginResponse);
        String loginToken = loginResponse.jsonPath().getString("token");

        Response otpResponse = post("/v1/auth/otp/verify",
            Map.of("otp", user.getOtp(), "token", loginToken));
        assertSuccess(otpResponse);

        setAuthToken(otpResponse.jsonPath().getString("access_token"));
        setBusinessUuid(otpResponse.jsonPath().getString("business_uuid"));
        String personUuid = otpResponse.jsonPath().getString("person_uuid");
        if (personUuid != null) setPersonUuid(personUuid);

        Log.pass(config, "Authenticated: " + user.getUsername());
    }
}
