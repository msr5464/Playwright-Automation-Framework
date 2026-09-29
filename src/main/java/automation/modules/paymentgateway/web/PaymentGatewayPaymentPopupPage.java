package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;

public class PaymentGatewayPaymentPopupPage extends BasePage
{
    private final Locator detailsIcon;
    private final Locator orderDetailsOverlay;
    private final Locator orderDetailsNameText;
    private final Locator orderDetailsPhoneText;
    private final Locator closeOverlayButton;
    private final Locator amountDisplay;
    private final Locator creditCardMethodOption;

    public PaymentGatewayPaymentPopupPage(Config config)
    {
        super(config);
        detailsIcon = page.frameLocator("#snap-midtrans").locator("span.header-detail");
        orderDetailsOverlay = page.frameLocator("#snap-midtrans").locator("div.order-summary-header");
        orderDetailsNameText = page.frameLocator("#snap-midtrans").locator("div.order-customer-group > div:first-child");
        orderDetailsPhoneText = page.frameLocator("#snap-midtrans").locator("div.order-summary-phone");
        closeOverlayButton = page.frameLocator("#snap-midtrans").locator(".header-modal-content .close-snap-button");
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        creditCardMethodOption = page.frameLocator("#snap-midtrans").locator("a[href='#/credit-card']");
        assertPageLoaded(amountDisplay);
    }

    public void openDetailsOverlay()
    {
        Log.comment(config, "Opening order details overlay via details icon");
        click(detailsIcon, "Details icon");
        WaitHelper.waitForElementToBeVisible(config, orderDetailsOverlay, "Order details overlay");
    }

    public String getOrderDetailsName()
    {
        return getText(orderDetailsNameText, "Order details name");
    }

    public String getOrderDetailsPhone()
    {
        return getText(orderDetailsPhoneText, "Order details phone");
    }

    public void closeOverlay()
    {
        Log.comment(config, "Closing order details overlay");
        click(closeOverlayButton, "Close overlay button");
        WaitHelper.waitForElementToBeHidden(config, orderDetailsOverlay, "Order details overlay");
    }

    public String getDisplayedAmount()
    {
        return normalizeAmount(getText(amountDisplay, "Payment popup amount display"));
    }

    public PaymentGatewayCreditCardPage selectCreditCardMethod()
    {
        Log.comment(config, "Selecting Credit Card as the payment method");
        click(creditCardMethodOption, "Credit Card payment method option");
        return new PaymentGatewayCreditCardPage(config);
    }

    /**
     * Normalizes a Midtrans-displayed amount (e.g. "Rp50.000" or "49000.00") into a
     * plain digit-only number string with no currency symbol, no thousands grouping
     * separator and no trailing decimal zeros (e.g. "50000", "49000").
     */
    public static String normalizeAmount(String raw)
    {
        if (raw == null)
        {
            return null;
        }
        String digitsAndDots = raw.replaceAll("[^0-9.]", "");
        int lastDot = digitsAndDots.lastIndexOf('.');
        if (lastDot >= 0)
        {
            String afterDot = digitsAndDots.substring(lastDot + 1);
            if (afterDot.length() == 3)
            {
                digitsAndDots = digitsAndDots.replace(".", "");
            }
            else
            {
                digitsAndDots = digitsAndDots.substring(0, lastDot).replace(".", "");
            }
        }
        return digitsAndDots;
    }

    /**
     * Normalizes a phone number into digits-only, stripping a leading Indonesian
     * country code ("62") or a leading trunk zero ("0") so that "+6281234567890"
     * and "081234567890" both normalize to "81234567890" for comparison.
     */
    public static String normalizePhoneDigits(String raw)
    {
        if (raw == null)
        {
            return null;
        }
        String digitsOnly = raw.replaceAll("[^0-9]", "");
        if (digitsOnly.startsWith("62"))
        {
            return digitsOnly.substring(2);
        }
        if (digitsOnly.startsWith("0"))
        {
            return digitsOnly.substring(1);
        }
        return digitsOnly;
    }
}
