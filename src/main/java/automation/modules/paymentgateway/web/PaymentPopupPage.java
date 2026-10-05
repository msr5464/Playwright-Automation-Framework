package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;

public class PaymentPopupPage extends BasePage
{
    private final Locator detailsIcon;
    private final Locator amountDisplay;

    public PaymentPopupPage(Config config)
    {
        super(config);
        detailsIcon = page.frameLocator("#snap-midtrans").locator("div.header-detail-clickable");
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        assertPageLoaded(amountDisplay);
    }

    public OrderDetailsOverlay openOrderDetails()
    {
        click(detailsIcon, "Order details icon");
        return new OrderDetailsOverlay(config);
    }

    public String getDisplayedAmount()
    {
        return getText(amountDisplay, "Amount display");
    }

    /**
     * Selects the payment method on the popup. Its locator is the confirmed
     * selector with only the option's key replaced, so every option is
     * selectable through the same code.
     */
    public PaymentPopupPage choosePaymentMethod(PaymentMethod method)
    {
        Locator paymentMethodOption = page.frameLocator("#snap-midtrans").locator("a[href='" + method.getKey() + "']");
        click(paymentMethodOption, method.getLabel() + " payment method");
        return this;
    }
}
