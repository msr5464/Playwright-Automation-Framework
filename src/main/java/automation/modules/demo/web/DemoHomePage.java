package automation.modules.demo.web;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;
import com.microsoft.playwright.Locator;

public class DemoHomePage extends BasePage
{
    private final Locator pageTitle;

    public DemoHomePage(Config config)
    {
        super(config);
        this.pageTitle = page.locator("h1");
        waitUntilLoaded();
    }

    @Override
    protected void waitUntilLoaded()
    {
        WaitHelper.waitForElementToBeVisible(config, pageTitle, "Demo page title");
    }

    public String getPageTitle()
    {
        return getText(pageTitle, "Demo page title");
    }

    public boolean isTitleContainingExample()
    {
        String title = getPageTitle();
        return title != null && title.contains("Example");
    }
}
