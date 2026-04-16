package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class HomePage extends BasePage
{

    private final Locator signInButton;

    public HomePage(Config config)
    {
        super(config);
        signInButton = page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
            new com.microsoft.playwright.Page.GetByRoleOptions().setName("Sign in"));
        assertPageLoaded(signInButton);
    }

    public LoginPage clickSignIn()
    {
        click(signInButton, "Sign In button");
        return new LoginPage(config);
    }
}
