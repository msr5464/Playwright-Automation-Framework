package automation.modules.toolshop.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.WaitHelper;

public class InvoiceDetailsPage extends BasePage {
    private final Locator invoiceNumberDisplay;
    private final Locator totalDisplay;
    private final Locator streetDisplay;
    private final Locator cityDisplay;
    private final Locator postalCodeDisplay;
    private final Locator paymentMethodDisplay;
    private final Locator cardHolderNameDisplay;
    private final Locator productsTable;
    private final Locator userMenu;
    private final Locator signOutLink;

    public InvoiceDetailsPage(Config config) {
        super(config);
        invoiceNumberDisplay = page.locator("[data-test='invoice-number']");
        totalDisplay = page.locator("[data-test='mukesh']");
        streetDisplay = page.locator("[data-test='street']");
        cityDisplay = page.locator("[data-test='city']");
        postalCodeDisplay = page.locator("[data-test='postal_code']");
        paymentMethodDisplay = page.locator("[data-test='payment-method']");
        cardHolderNameDisplay = page.locator("#card_holder_name");
        productsTable = page.locator("div.row.mt-3 table.table.table-hover");
        userMenu = page.locator("[data-test='nav-mukesh']");
        signOutLink = page.locator("[data-test='nav-sign-out']");
        assertPageLoaded(invoiceNumberDisplay);
    }

    public String getInvoiceNumber() {
        return getInputValue(invoiceNumberDisplay, "Invoice number");
    }

    public String getTotal() {
        return getInputValue(totalDisplay, "Total").replaceAll("\\s+", "");
    }

    public String getStreet() {
        return getInputValue(streetDisplay, "Street");
    }

    public String getCity() {
        return getInputValue(cityDisplay, "City");
    }

    public String getPostalCode() {
        return getInputValue(postalCodeDisplay, "Postal code");
    }

    public String getPaymentMethod() {
        return getInputValue(paymentMethodDisplay, "Payment method");
    }

    public String getCardHolderName() {
        return getInputValue(cardHolderNameDisplay, "Card holder name");
    }

    public String getProductRowQuantity(String productName) {
        Locator row = productsTable.locator("tbody tr").filter(new Locator.FilterOptions().setHasText(productName));
        Locator quantityCell = row.locator("td").nth(0);
        return getText(quantityCell, "Quantity for " + productName);
    }

    public HomePage signOut() {
        click(userMenu, "User menu");
        WaitHelper.waitForElementToBeVisible(config, signOutLink, "Sign out link");
        click(signOutLink, "Sign out link");
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("toolshop.url"));
        return new HomePage(config);
    }
}
