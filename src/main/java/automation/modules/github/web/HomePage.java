package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

public class HomePage extends BasePage
{

    private final Locator signInButton;

    public HomePage(Config config)
    {
        super(config);
        signInButton = page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
            new com.microsoft.playwright.Page.GetByRoleOptions().setName("Sign in"));
        waitUntilLoaded();
    }

    @Override
    protected void waitUntilLoaded()
    {
        WaitHelper.waitForElementToBeVisible(config, signInButton, "Sign In button");
    }

    public LoginPage clickSignIn()
    {
        click(signInButton, "Sign In button");
        return new LoginPage(config);
    }
}
