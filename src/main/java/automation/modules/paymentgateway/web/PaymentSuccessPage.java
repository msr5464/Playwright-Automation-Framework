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

    public String getAmount()
    {
        return normalizeAmount(getText(successAmount, "Success amount"));
    }

    public String getOrderId()
    {
        return getText(successOrderId, "Success order id");
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
