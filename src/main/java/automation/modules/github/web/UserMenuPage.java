package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

/**
 * Page object for the GitHub user navigation menu dialog.
 * Opened by clicking the user avatar/menu trigger on the dashboard.
 */
public class UserMenuPage extends BasePage
{
    private final Locator userNameLabel;

    public UserMenuPage(Config config)
    {
        super(config);
        userNameLabel = page.locator("dialog[aria-label='User navigation'] span").first();
        waitUntilLoaded();
    }

    @Override
    protected void waitUntilLoaded()
    {
        WaitHelper.waitForElementToBeVisible(config, userNameLabel, "User name label");
    }

    /**
     * Returns the displayed user name text from the user navigation menu.
     */
    public String getUserName()
    {
        return getText(userNameLabel, "User name label");
    }
}
