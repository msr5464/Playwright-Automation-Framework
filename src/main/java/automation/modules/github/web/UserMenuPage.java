package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

/**
 * Represents the user navigation menu dialog that appears after clicking the avatar/menu button.
 * Provides access to the display name and login handle shown in the menu.
 */
public class UserMenuPage extends BasePage
{

    private final Locator userMenuDialog;
    private final Locator userNameText;
    private final Locator userLoginText;
    private final Locator closeButton;

    public UserMenuPage(Config config)
    {
        super(config);
        userMenuDialog = page.locator("dialog[aria-label='User navigation']");
        userNameText   = page.locator(".p-name");
        userLoginText  = page.locator(".p-login");
        closeButton    = page.locator("button[aria-label='Close user navigation menu']");
        assertPageLoaded(userMenuDialog);
    }

    /**
     * Return the display name shown in the user navigation menu.
     */
    public String getUserName()
    {
        return getText(userNameText, "User display name");
    }

    /**
     * Return the login handle shown in the user navigation menu.
     */
    public String getUserLogin()
    {
        return getText(userLoginText, "User login handle");
    }

    /**
     * Close the user navigation menu dialog.
     */
    public void close()
    {
        click(closeButton, "Close user navigation menu button");
    }
}
