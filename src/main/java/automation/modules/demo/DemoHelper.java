package automation.modules.demo;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.demo.web.DemoHomePage;

public class DemoHelper extends ApiHelper
{
    private static final String BASE_URL = "https://example.com";

    public DemoHelper(Config config)
    {
        super(config, BASE_URL);
    }

    public DemoHomePage visitHomePage()
    {
        Log.comment(config, "Navigating to Demo home page: " + BASE_URL);
        BrowserHelper.navigateTo(config, BASE_URL);
        return new DemoHomePage(config);
    }
}
