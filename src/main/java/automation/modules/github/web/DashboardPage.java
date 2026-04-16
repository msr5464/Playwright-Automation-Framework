package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class DashboardPage extends BasePage
{

    private final Locator avatarWidget;
    private final Locator userMenu;

    public DashboardPage(Config config)
    {
        super(config);
        avatarWidget = page.locator("img[class*='avatar']").first();
        userMenu     = page.locator("summary[aria-label*='View profile'], .AppHeader-user");
        assertPageLoaded(avatarWidget);
    }

    public boolean isLoggedIn()
    {
        return isElementDisplayed(avatarWidget);
    }

    public void openUserMenu()
    {
        click(userMenu, "User menu");
    }

    /**
     * Click the user navigation menu and return the resulting UserMenuPage.
     */
    public UserMenuPage clickUserMenu()
    {
        click(userMenu, "User menu");
        return new UserMenuPage(config);
    }
}
