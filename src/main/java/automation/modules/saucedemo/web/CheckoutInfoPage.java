package automation.modules.saucedemo.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class CheckoutInfoPage extends BasePage
{
    private final Locator pageTitle;
    private final Locator firstNameField;
    private final Locator lastNameField;
    private final Locator zipCodeField;
    private final Locator continueButton;
    private final Locator cancelButton;

    public CheckoutInfoPage(Config config)
    {
        super(config);
        pageTitle      = page.locator(".title");
        firstNameField = page.locator("[data-test='firstName']");
        lastNameField  = page.locator("[data-test='lastName']");
        zipCodeField   = page.locator("[data-test='postalCode']");
        continueButton = page.locator("[data-test='continue']");
        cancelButton   = page.locator("[data-test='cancel']");
        assertPageLoaded(pageTitle);
    }

    public void fillFirstName(String firstName)
    {
        fillText(firstNameField, firstName, "First name field");
    }

    public void fillLastName(String lastName)
    {
        fillText(lastNameField, lastName, "Last name field");
    }

    public void fillZipCode(String zipCode)
    {
        fillText(zipCodeField, zipCode, "Zip code field");
    }

    /**
     * Fills all checkout information fields in one call.
     */
    public void fillCheckoutInfo(String firstName, String lastName, String zipCode)
    {
        fillText(firstNameField, firstName, "First name field");
        fillText(lastNameField, lastName, "Last name field");
        fillText(zipCodeField, zipCode, "Zip code field");
    }

    /**
     * Submits the checkout information form and navigates to the Checkout Overview page.
     */
    public CheckoutOverviewPage clickContinue()
    {
        click(continueButton, "Continue button");
        return new CheckoutOverviewPage(config);
    }

    /**
     * Cancels checkout and returns to the Cart page.
     */
    public CartPage clickCancel()
    {
        click(cancelButton, "Cancel button");
        return new CartPage(config);
    }
}
