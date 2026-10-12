package automation.modules.toolshop.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;

public class CheckoutConfirmationPage extends BasePage {
    private final Locator confirmationMessage;
    private final Locator userMenu;
    private final Locator myInvoicesLink;

    public CheckoutConfirmationPage(Config config) {
        super(config);
        confirmationMessage = page.locator("#order-confirmation");
        userMenu = page.locator("[data-test='nav-mukesh']");
        myInvoicesLink = page.locator("[data-test='nav-my-invoices']");
        assertPageLoaded(confirmationMessage);
    }

    public String getConfirmationMessage() {
        return getText(confirmationMessage, "Order confirmation message");
    }

    public String getInvoiceNumber() {
        String message = getConfirmationMessage();
        int index = message.lastIndexOf("is");
        String invoiceNumber = index >= 0 ? message.substring(index + 2) : message;
        return invoiceNumber.replace(".", "").trim();
    }

    public MyInvoicesPage openMyInvoices() {
        click(userMenu, "User menu");
        WaitHelper.waitForElementToBeVisible(config, myInvoicesLink, "My invoices link");
        click(myInvoicesLink, "My invoices link");
        return new MyInvoicesPage(config);
    }
}
