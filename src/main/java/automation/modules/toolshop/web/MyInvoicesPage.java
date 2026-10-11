package automation.modules.toolshop.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class MyInvoicesPage extends BasePage
{
    private final Locator firstRowInvoiceNumber;
    private final Locator firstRowTotal;
    private final Locator firstRowDetailsButton;

    public MyInvoicesPage(Config config)
    {
        super(config);
        firstRowInvoiceNumber = page.locator("table tbody tr:first-child td:nth-child(1)");
        firstRowTotal = page.locator("table tbody tr:first-child td:nth-child(4)");
        firstRowDetailsButton = page.locator("table tbody tr:first-child a:has-text('Details')");
        assertPageLoaded(firstRowInvoiceNumber);
    }

    public String getFirstRowInvoiceNumber()
    {
        return getText(firstRowInvoiceNumber, "First row invoice number");
    }

    public String getFirstRowTotal()
    {
        return getText(firstRowTotal, "First row total");
    }

    public InvoiceDetailsPage openDetails()
    {
        click(firstRowDetailsButton, "Details button");
        return new InvoiceDetailsPage(config);
    }
}
