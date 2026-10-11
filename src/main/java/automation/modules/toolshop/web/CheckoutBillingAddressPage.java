package automation.modules.toolshop.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;
import automation.modules.toolshop.ToolshopData;

public class CheckoutBillingAddressPage extends BasePage
{
    private final Locator countryField;
    private final Locator postalCodeField;
    private final Locator houseNumberField;
    private final Locator streetField;
    private final Locator cityField;
    private final Locator stateField;
    private final Locator proceedToCheckoutButton;

    public CheckoutBillingAddressPage(Config config)
    {
        super(config);
        countryField = page.locator("[data-test='country']");
        postalCodeField = page.locator("[data-test='postal_code']");
        houseNumberField = page.locator("[data-test='house_number']");
        streetField = page.locator("[data-test='street']");
        cityField = page.locator("[data-test='city']");
        stateField = page.locator("[data-test='state']");
        proceedToCheckoutButton = page.locator("[data-test='proceed-3']");
        assertPageLoaded(countryField);
    }

    public CheckoutBillingAddressPage fillBillingAddress(ToolshopData order)
    {
        selectOption(countryField, order.getCountry(), "Country field");
        fillText(postalCodeField, order.getPostalCode(), "Postal code field");
        fillText(houseNumberField, order.getHouseNumber(), "House number field");
        WaitHelper.waitForPageToSettle(config);
        return this;
    }

    public String getStreet()
    {
        return getInputValue(streetField, "Street field");
    }

    public String getCity()
    {
        return getInputValue(cityField, "City field");
    }

    public CheckoutPaymentPage proceedToCheckout()
    {
        click(proceedToCheckoutButton, "Proceed to checkout button");
        return new CheckoutPaymentPage(config);
    }
}
