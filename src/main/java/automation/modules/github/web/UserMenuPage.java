package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

public class UserMenuPage extends BasePage
{
    private final Locator drawerPanel;
    private final Locator userNameLabel;
    private final Locator userLoginLabel;

    public UserMenuPage(Config config)
    {
        super(config);
        drawerPanel = page.locator(
            "[data-target='user-drawer-side-panel-manager.panel'], " +
            "[role='dialog'], " +
            "[role='menu']"
        ).first();
        userNameLabel = page.locator(
            "[data-target='user-drawer-side-panel-manager.panel'] .p-name, " +
            ".p-name, " +
            "[role='dialog'] strong, " +
            "[role='menu'] strong"
        ).first();
        userLoginLabel = page.locator(
            "[data-target='user-drawer-side-panel-manager.panel'] .p-nickname, " +
            ".p-nickname, " +
            "[data-login], " +
            "[role='dialog'] .color-fg-muted, " +
            "[role='menu'] .color-fg-muted"
        ).first();
        waitUntilLoaded();
    }

    @Override
    protected void waitUntilLoaded()
    {
        WaitHelper.waitForPageLoad(config);
        WaitHelper.waitForNetworkIdle(config);
        WaitHelper.waitForOptionalElementToBeVisible(config, drawerPanel, "User drawer panel");
    }

    public String getDisplayedName()
    {
        return getText(userNameLabel, "User name label");
    }

    public String getDisplayedLogin()
    {
        return getText(userLoginLabel, "User login label");
    }
}
