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
        userMenu     = page.locator("button[aria-label*='profile'], button[aria-label*='navigation menu'], summary[aria-label*='View profile'], button:has(img[class*='avatar']), .AppHeader-user").first();
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

    /**
     * Opens the user navigation menu and returns the resulting UserMenuPage.
     */
    public UserMenuPage openUserMenu()
    {
        click(userMenu, "User menu");
        return new UserMenuPage(config);
    }
}
