package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

public class PaymentSuccessPage extends BasePage
{
    private final Locator successAmount;
    private final Locator successOrderId;

    public PaymentSuccessPage(Config config)
    {
        super(config);
        successAmount = page.frameLocator("#snap-midtrans").locator("div.text-headline.large");
        successOrderId = page.frameLocator("#snap-midtrans").locator("div.text-info.small");
        assertPageLoaded(successAmount);
    }

    /**
     * Returns the amount and order id shown on the payment successful page as a pair,
     * since this page closes almost immediately after the OTP is submitted and both
     * values must be captured in the same call. The amount is plain number text
     * (no currency symbol, no grouping separators, no trailing decimal zeros).
     */
    public String[] getSuccessDetails()
    {
        String rawAmount = getText(successAmount, "Success amount");
        String amount = rawAmount.replaceAll("[^0-9.]", "");
        if (amount.contains("."))
        {
            amount = amount.replaceAll("\\.0+$", "");
            amount = amount.replaceAll("\\.$", "");
        }
        String orderId = getText(successOrderId, "Success order id");
        return new String[] { amount, orderId };
    }
}
