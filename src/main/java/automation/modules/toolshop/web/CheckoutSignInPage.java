package automation.modules.toolshop.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

public class CheckoutSignInPage extends BasePage
{
    private final Locator emailField;
    private final Locator passwordField;
    private final Locator signInButton;
    private final Locator loggedInMessage;
    private final Locator proceedToCheckoutButton;

    public CheckoutSignInPage(Config config)
    {
        super(config);
        emailField = page.locator("[data-test='email']");
        passwordField = page.locator("[data-test='password']");
        signInButton = page.locator("[data-test='login-submit']");
        loggedInMessage = page.locator("p.ng-star-inserted");
        proceedToCheckoutButton = page.locator("[data-test='proceed-2']");
        assertPageLoaded(emailField);
    }

    public CheckoutSignInPage signIn(String email, String password)
    {
        fillText(emailField, email, "Email field");
        fillText(passwordField, password, "Password field");
        click(signInButton, "Sign in button");
        WaitHelper.waitForPageToSettle(config);
        return this;
    }

    public String getLoggedInMessage()
    {
        return getText(loggedInMessage, "Logged in message");
    }

    public CheckoutBillingAddressPage proceedToCheckout()
    {
        click(proceedToCheckoutButton, "Proceed to checkout button");
        return new CheckoutBillingAddressPage(config);
    }
}
