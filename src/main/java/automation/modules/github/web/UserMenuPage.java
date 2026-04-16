package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

/**
 * Represents the GitHub user navigation menu dialog.
 * Opened by clicking the user avatar in the header on DashboardPage.
 */
public class UserMenuPage extends BasePage
{

    private final Locator userNameText;
    private final Locator userLoginText;
    private final Locator closeButton;

    public UserMenuPage(Config config)
    {
        super(config);
        userNameText = page.locator("dialog[aria-label='User navigation'] .p-name");
        userLoginText = page.locator("dialog[aria-label='User navigation'] .p-nickname");
        closeButton   = page.locator("button[aria-label='Close user navigation menu']");
        waitUntilLoaded();
    }

    @Override
    protected void waitUntilLoaded()
    {
        WaitHelper.waitForElementToBeVisible(config, userNameText, "User name in navigation menu");
    }

    /**
     * Returns the display name shown in the user navigation menu.
     */
    public String getUserName()
    {
        return getText(userNameText, "User name");
    }

    /**
     * Returns the login (username) shown in the user navigation menu.
     */
    public String getUserLogin()
    {
        return getText(userLoginText, "User login");
    }

    /**
     * Closes the user navigation menu.
     */
    public void close()
    {
        click(closeButton, "Close user navigation menu button");
    }
}
