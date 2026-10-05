package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Element;
import automation.core.WaitHelper;
import automation.modules.paymentgateway.PaymentGatewayEnums.PaymentMethod;

public class SnapPaymentPage extends BasePage
{
    private final Locator detailsIcon;
    private final Locator orderDetailsName;
    private final Locator orderDetailsPhone;
    private final Locator closeOverlayButton;
    private final Locator amountDisplay;

    public SnapPaymentPage(Config config)
    {
        super(config);
        detailsIcon = page.frameLocator("#snap-midtrans").locator("div.header-detail-clickable");
        orderDetailsName = page.frameLocator("#snap-midtrans").locator("div.order-customer-group > div:first-child");
        orderDetailsPhone = page.frameLocator("#snap-midtrans").locator("div.order-summary-phone");
        // While the overlay is open, a full-screen backdrop covers the header toggle and is
        // itself the widget's click-outside-to-dismiss target, so close via the backdrop.
        closeOverlayButton = page.frameLocator("#snap-midtrans").locator("div.outside-area");
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        assertPageLoaded(amountDisplay);
    }

    public SnapPaymentPage openOrderDetailsOverlay()
    {
        click(detailsIcon, "Order details icon");
        WaitHelper.waitForElementToBeVisible(config, orderDetailsName, "Order details name");
        return this;
    }

    public SnapPaymentPage closeOrderDetailsOverlay()
    {
        // The backdrop spans the full viewport, so a click at its bounding-box center
        // lands on the still-open modal content sitting on top of it there; click its
        // corner instead, which the modal does not cover.
        Element.clickViaCoordinates(config, closeOverlayButton, "Close order details overlay");
        return this;
    }

    public String getOrderDetailsName()
    {
        return getText(orderDetailsName, "Order details name");
    }

    public String getOrderDetailsPhone()
    {
        return getText(orderDetailsPhone, "Order details phone");
    }

    public String getAmount()
    {
        return normalizeAmount(getText(amountDisplay, "Amount display"));
    }

    public CreditCardFormPage choosePaymentMethod(PaymentMethod method)
    {
        Locator paymentMethodOption = page.frameLocator("#snap-midtrans").locator("a[href='" + method.getKey() + "']");
        click(paymentMethodOption, method.getLabel() + " payment method");
        return new CreditCardFormPage(config);
    }

    /**
     * Reduces a displayed amount ('Rp50.000', '49000.00') to plain number text
     * ('50000', '49000') - no currency symbol, no thousands separator, no trailing
     * decimal zeros - so two differently-formatted amounts can be compared as strings.
     */
    private String normalizeAmount(String rawAmount)
    {
        String digitsAndDot = rawAmount.replaceAll("[^0-9.]", "");
        int lastDot = digitsAndDot.lastIndexOf('.');
        if (lastDot == -1)
        {
            return digitsAndDot;
        }
        String afterDot = digitsAndDot.substring(lastDot + 1);
        if (afterDot.length() == 2)
        {
            return digitsAndDot.substring(0, lastDot);
        }
        return digitsAndDot.replace(".", "");
    }
}
