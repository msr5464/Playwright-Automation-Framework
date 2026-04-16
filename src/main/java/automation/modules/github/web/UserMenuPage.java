package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

/**
 * Represents the GitHub user navigation dialog that appears after clicking the header avatar/menu.
 * Exposes the display name, login handle, and a close action.
 */
public class UserMenuPage extends BasePage
{

    private final Locator dialog;
    private final Locator userNameText;
    private final Locator userLoginText;
    private final Locator closeButton;

    public UserMenuPage(Config config)
    {
        super(config);
        dialog       = page.locator("dialog[aria-label='User navigation']");
        userNameText = page.locator("dialog[aria-label='User navigation'] .p-name");
        userLoginText = page.locator("dialog[aria-label='User navigation'] .p-nickname");
        closeButton  = page.locator("button[aria-label='Close user navigation menu']");
        assertPageLoaded(dialog);
    }

    /**
     * Returns the display name shown in the user navigation dialog.
     */
    public String getUserName()
    {
        return getText(userNameText, "User display name");
    }

    /**
     * Returns the login handle shown in the user navigation dialog.
     */
    public String getUserLogin()
    {
        return getText(userLoginText, "User login");
    }

    /**
     * Closes the user navigation dialog.
     */
    public void close()
    {
        click(closeButton, "Close user navigation menu button");
    }
}
