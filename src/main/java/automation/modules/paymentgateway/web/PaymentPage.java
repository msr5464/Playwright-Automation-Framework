package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class PaymentPage extends BasePage
{
    private final Locator amountDisplay;
    private final Locator detailsIcon;
    private final Locator creditCardTab;

    public PaymentPage(Config config)
    {
        super(config);
        amountDisplay = page.frameLocator("#snap-midtrans").locator(".header-amount");
        detailsIcon   = page.frameLocator("#snap-midtrans").locator(".header-detail-clickable");
        creditCardTab = page.frameLocator("#snap-midtrans").locator("#credit_card a[data-testid=\"list-item\"]");
        assertPageLoaded(amountDisplay);
    }

    public OrderDetailsOverlay clickDetails()
    {
        Log.comment(config, "Clicking Details icon to open order details overlay");
        click(detailsIcon, "Details icon");
        return new OrderDetailsOverlay(config);
    }

    public String getAmount()
    {
        return getText(amountDisplay, "Amount display");
    }

    public CreditCardFormPage selectCreditCard()
    {
        Log.comment(config, "Selecting Credit Card as payment method");
        click(creditCardTab, "Credit Card tab");
        return new CreditCardFormPage(config);
    }
}
