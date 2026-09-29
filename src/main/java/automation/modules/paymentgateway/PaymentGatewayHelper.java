package automation.modules.paymentgateway;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.paymentgateway.web.MidtransLandingPage;

import java.util.regex.Pattern;

/**
 * Helper for the PaymentGateway module (Midtrans demo checkout).
 * This module is web-only today; the ApiHelper base is retained so the module
 * follows the standard Helper shape and can pick up API endpoints later without
 * a structural change.
 *
 * Web usage:
 *   PaymentGatewayHelper paymentGateway = new PaymentGatewayHelper(config);
 *   MidtransLandingPage landing = paymentGateway.navigateToLandingPage();
 */
public class PaymentGatewayHelper extends ApiHelper
{
    private static final Pattern DECIMAL_SUFFIX = Pattern.compile("\\d+\\.\\d{2}$");

    public PaymentGatewayHelper(Config config)
    {
        super(config, config.getRunTimeProperty("paymentgateway.url"));
    }

    /**
     * Navigate to the Midtrans demo checkout landing page and return its page object.
     */
    public MidtransLandingPage navigateToLandingPage()
    {
        String url = config.getRunTimeProperty("paymentgateway.url");
        Log.comment(config, "Navigating to Midtrans demo checkout: " + url);
        BrowserHelper.navigateTo(config, url);
        return new MidtransLandingPage(config);
    }

    /**
     * Normalize a displayed amount string into plain digits for comparison, regardless of
     * whether it was rendered with a currency prefix and thousands separators (e.g. "Rp50.000")
     * or as a decimal amount (e.g. "49000.00"). Distinguishes the two by counting the digits
     * after the final '.': exactly 2 digits is treated as a decimal fraction and dropped,
     * any other count is treated as a thousands separator and the dot is removed entirely.
     * The result has no currency symbol, no grouping separators and no trailing decimal zeros.
     */
    public String extractPlainAmount(String displayedAmount)
    {
        Log.comment(config, "Normalizing displayed amount for comparison: " + displayedAmount);
        String trimmed = displayedAmount.trim();
        String normalized;
        if (DECIMAL_SUFFIX.matcher(trimmed).find())
        {
            normalized = trimmed.substring(0, trimmed.lastIndexOf('.'));
        }
        else
        {
            normalized = trimmed.replace(".", "");
        }
        normalized = normalized.replaceAll("[^0-9]", "");
        Log.comment(config, "Normalized amount: " + normalized);
        return normalized;
    }
}
