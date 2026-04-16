package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

public class DashboardPage extends BasePage
{

    private final Locator avatarWidget;
    private final Locator userMenu;
    private final Locator userMenuSummary;
    private final Locator userNameDisplay;

    public DashboardPage(Config config)
    {
        super(config);
        avatarWidget    = page.locator("img[class*='avatar']").first();
        userMenu        = page.locator("button[aria-label='Open user navigation menu']");
        userMenuSummary = page.locator("heading[aria-label='User navigation']");
        userNameDisplay = page.locator("dialog[aria-label='User navigation'] h1");
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

    /**
     * Get the display name of the currently logged-in user from the user navigation menu.
     * Requires openUserMenu() to be called first so the element is visible.
     *
     * @return the user's display name as shown in the navigation menu
     */
    public String getUserDisplayName()
    {
        WaitHelper.waitForElementToBeVisible(config, userNameDisplay, "User name display");
        return getText(userNameDisplay, "User name display");
    }
}
