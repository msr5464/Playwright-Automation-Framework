package automation.modules.paymentgateway.web;

import com.microsoft.playwright.FrameLocator;
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
        FrameLocator snapFrame = page.frameLocator("#snap-midtrans");
        amountDisplay = snapFrame.locator(".header-amount");
        detailsIcon   = snapFrame.locator(".header-detail-clickable");
        creditCardTab = snapFrame.locator("#credit_card a[data-testid='list-item']");
        assertPageLoaded(amountDisplay);
    }

    public OrderDetailsOverlay clickDetails()
    {
        Log.comment(config, "Clicking order details icon");
        click(detailsIcon, "Details icon");
        return new OrderDetailsOverlay(config);
    }

    public String getAmount()
    {
        return getText(amountDisplay, "Amount display");
    }

    public CreditCardFormPage selectCreditCard()
    {
        Log.comment(config, "Selecting Credit Card payment method");
        click(creditCardTab, "Credit Card tab");
        return new CreditCardFormPage(config);
    }
}
