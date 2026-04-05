package automation.modules.saucedemo;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.TestDataReader;
import automation.core.api.ApiHelper;
import automation.modules.saucedemo.web.LoginPage;
import automation.modules.saucedemo.web.ProductsPage;

import java.util.Map;

/**
 * Unified helper for SauceDemo web flows and JSONPlaceholder API flows.
 * Extends ApiHelper with the JSONPlaceholder base URL (external API — no app auth).
 * Web credentials are loaded from config properties, not the user pool.
 *
 * API usage:
 *   SauceDemoHelper api = new SauceDemoHelper(config);
 *   PostData created = api.execute(PostApi.CreatePost, post, PostData.class);
 *   PostData fetched = api.execute(PostApi.GetPost.withPath("id", "1"), PostData.class);
 *   api.execute(PostApi.DeletePost.withPath("id", "1"));
 *
 * Web usage:
 *   SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
 *   ProductsPage products = sauceDemo.doLogin();
 *   products.addProductToCart("sauce-labs-backpack");
 *   CartPage cart = products.goToCart();
 */
public class SauceDemoHelper extends ApiHelper
{
    private static final String API_BASE_URL = "https://jsonplaceholder.typicode.com";

    public SauceDemoHelper(Config config)
    {
        super(config, API_BASE_URL);
    }

    /**
     * Login using credentials from config properties (saucedemo.username / saucedemo.password).
     */
    public ProductsPage doLogin()
    {
        return doLogin(
            config.getRunTimeProperty("saucedemo.username"),
            config.getRunTimeProperty("saucedemo.password")
        );
    }

    public ProductsPage doLogin(Map<String, String> credentials)
    {
        return doLogin(credentials.get("username"), credentials.get("password"));
    }

    public ProductsPage doLogin(String username, String password)
    {
        String url = config.getRunTimeProperty("saucedemo.url");
        Log.comment(config, "Navigating to SauceDemo: " + url);
        BrowserHelper.navigateTo(config, url);
        return new LoginPage(config).doLogin(username, password);
    }

    /**
     * Load credentials for a scenario from CSV, matched to the current environment.
     * CSV: src/test/resources/saucedemo/csvFiles/saucedemo-testdata.csv
     * Columns: scenario, environment, username, password, role
     */
    public Map<String, String> getCredentials(String scenario)
    {
        return TestDataReader.loadCsvRowByColumnValue(
            "saucedemo", "saucedemo-testdata", "scenario", scenario, Config.environment);
    }
}
