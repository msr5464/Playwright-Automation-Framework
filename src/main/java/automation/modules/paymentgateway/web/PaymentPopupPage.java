package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;

public class PaymentPopupPage extends BasePage
{
    private final Locator detailsIcon;
    private final Locator closeOverlayButton;
    private final Locator amountText;
    private final Locator orderDetailsNameText;
    private final Locator orderDetailsPhoneText;

    public PaymentPopupPage(Config config)
    {
        super(config);
        detailsIcon = page.frameLocator("#snap-midtrans").locator("span.header-detail");
        closeOverlayButton = page.frameLocator("#snap-midtrans").locator(".header-modal-content .close-snap-button");
        amountText = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        orderDetailsNameText = page.frameLocator("#snap-midtrans").locator(".order-customer-group div:first-child");
        orderDetailsPhoneText = page.frameLocator("#snap-midtrans").locator("div.order-summary-phone");
        assertPageLoaded(amountText);
    }

    public void clickDetailsIcon()
    {
        Log.comment(config, "Clicking Details icon to open the order details overlay");
        click(detailsIcon, "Details icon");
        WaitHelper.waitForElementToBeVisible(config, orderDetailsNameText, "Order details name");
    }

    public CreditCardPaymentPage closeOverlay()
    {
        Log.comment(config, "Closing the order details overlay");
        click(closeOverlayButton, "Close overlay button");
        return new CreditCardPaymentPage(config);
    }

    /**
     * Returns the amount shown on the payment popup as a plain number string
     * (no currency symbol, no grouping separators, no trailing decimal zeros)
     * so it can be compared for equality with amounts from other pages.
     */
    public String getAmountText()
    {
        String raw = getText(amountText, "Payment popup amount");
        return normalizeAmount(raw);
    }

    public String getOrderDetailsName()
    {
        return getText(orderDetailsNameText, "Order details name");
    }

    /**
     * Returns the phone number from the order details overlay as digits only,
     * with a leading country code (62) or trunk zero (0) dropped, so it can be
     * compared for equality with the phone number entered in the shopping cart form.
     */
    public String getOrderDetailsPhone()
    {
        String raw = getText(orderDetailsPhoneText, "Order details phone");
        return normalizePhoneDigits(raw);
    }

    private static String normalizeAmount(String raw)
    {
        String cleaned = raw.replace("Rp", "").trim();
        cleaned = cleaned.replace(",", "");
        if (cleaned.matches(".*\\.\\d{2}$"))
        {
            cleaned = cleaned.substring(0, cleaned.lastIndexOf('.'));
        }
        else
        {
            cleaned = cleaned.replace(".", "");
        }
        return cleaned;
    }

    private static String normalizePhoneDigits(String raw)
    {
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith("62"))
        {
            digits = digits.substring(2);
        }
        else if (digits.startsWith("0"))
        {
            digits = digits.substring(1);
        }
        return digits;
    }
}
