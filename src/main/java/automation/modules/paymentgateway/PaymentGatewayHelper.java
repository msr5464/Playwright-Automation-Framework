package automation.modules.paymentgateway;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.paymentgateway.web.HomePage;

/**
 * Helper for the PaymentGateway (Midtrans demo checkout) web module.
 * This is a web-only module - there is no application API to call, so this
 * class extends ApiHelper only to stay consistent with the framework's Helper
 * pattern (a base URL is still required by the constructor) but exposes no
 * API methods.
 *
 * Web usage:
 *   PaymentGatewayHelper paymentGateway = new PaymentGatewayHelper(config);
 *   HomePage home = paymentGateway.navigateToHomePage();
 *   PaymentGatewayData order = paymentGateway.buildOrderData();
 *   ... drive OrderFormPage -> PaymentPopupPage -> CreditCardPaymentPage -> IssuingBankPage -> PaymentSuccessPage directly ...
 *   String normalized = paymentGateway.normalizeAmount("Rp19.000"); // "19000"
 *   boolean same = paymentGateway.phoneNumbersMatch("081234567890", "+6281234567890"); // true
 */
public class PaymentGatewayHelper extends ApiHelper
{
    public PaymentGatewayHelper(Config config)
    {
        super(config, config.getRunTimeProperty("paymentgateway.url"));
    }

    /**
     * Navigate to the Midtrans demo home page and return the loaded page object.
     */
    public HomePage navigateToHomePage()
    {
        String url = config.getRunTimeProperty("paymentgateway.url");
        Log.comment(config, "Navigating to PaymentGateway demo site: " + url);
        BrowserHelper.navigateTo(config, url);
        return new HomePage(config);
    }

    /**
     * Build a fresh set of dummy order data (customer name, phone, and default
     * card/promo/OTP values) using the module's builder and its defaults.
     */
    public PaymentGatewayData buildOrderData()
    {
        Log.comment(config, "Building dummy order data for checkout");
        return new PaymentGatewayBuilder().build();
    }

    /**
     * Normalize a displayed amount string (e.g. "Rp19.000", "20,000", "19000.00")
     * down to plain digits with no currency symbol, no grouping separators, and
     * no trailing decimal zeros - so two amounts rendered differently across
     * pages can be compared as strings.
     */
    public String normalizeAmount(String rawAmount)
    {
        if (rawAmount == null)
        {
            return null;
        }
        Log.comment(config, "Normalizing displayed amount: " + rawAmount);
        String digitsOnly = rawAmount.replaceAll("[^0-9.]", "");
        if (digitsOnly.contains("."))
        {
            int firstDot = digitsOnly.indexOf('.');
            String integerPart = digitsOnly.substring(0, firstDot).replace(".", "");
            String fractionPart = digitsOnly.substring(firstDot + 1).replace(".", "");
            if (fractionPart.matches("0+"))
            {
                return integerPart;
            }
            return integerPart + fractionPart;
        }
        return digitsOnly;
    }

    /**
     * Extract the significant digits of a phone number, stripping a leading
     * country code (62) or trunk prefix (0) so numbers rendered differently
     * across pages ("081234567890" vs "+6281234567890") can be compared.
     */
    public String extractSignificantPhoneDigits(String phone)
    {
        if (phone == null)
        {
            return null;
        }
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.startsWith("62"))
        {
            digitsOnly = digitsOnly.substring(2);
        }
        else if (digitsOnly.startsWith("0"))
        {
            digitsOnly = digitsOnly.substring(1);
        }
        return digitsOnly;
    }

    /**
     * Compare two phone number strings for equality once a leading country
     * code (62) or trunk zero has been stripped from each.
     */
    public boolean phoneNumbersMatch(String phone1, String phone2)
    {
        Log.comment(config, "Comparing phone numbers: " + phone1 + " vs " + phone2);
        String digits1 = extractSignificantPhoneDigits(phone1);
        String digits2 = extractSignificantPhoneDigits(phone2);
        return digits1 != null && digits1.equals(digits2);
    }
}
