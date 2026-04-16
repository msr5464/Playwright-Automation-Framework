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
        userMenu     = page.locator("button[aria-label='Open user navigation menu'], button[aria-label*='user navigation'], .AppHeader-user button, [data-testid='header-account-toggle']").first();
        assertPageLoaded(avatarWidget);
    }

    public boolean isLoggedIn()
    {
        return isElementDisplayed(avatarWidget);
    }

    /**
     * Click the user navigation menu icon and return the resulting UserMenuPage.
     */
    public UserMenuPage openUserMenu()
    {
        click(userMenu, "User menu");
        return new UserMenuPage(config);
    }
}
