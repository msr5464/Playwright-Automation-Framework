package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

public class DashboardPage extends BasePage
{

    private final Locator avatarWidget;
    private final Locator userMenu;

    public DashboardPage(Config config)
    {
        super(config);
        avatarWidget = page.locator("img[class*='avatar']").first();
        userMenu     = page.locator("summary[aria-label*='View profile'], .AppHeader-user");
        waitUntilLoaded();
    }

    @Override
    protected void waitUntilLoaded()
    {
        WaitHelper.waitForElementToBeVisible(config, avatarWidget, "User avatar");
    }

    public boolean isLoggedIn()
    {
        return isElementDisplayed(avatarWidget);
    }

    public void openUserMenu()
    {
        click(userMenu, "User menu");
    }
}
