package automation.modules.paymentgateway.web;

import com.microsoft.playwright.Locator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.WaitHelper;
import automation.modules.paymentgateway.PaymentGatewayData;
import automation.modules.paymentgateway.PaymentGatewayEnums.Promo;

public class CreditCardFormPage extends BasePage
{
    private final Locator cardNumberField;
    private final Locator expiryField;
    private final Locator cvvField;
    private final Locator amountDisplay;
    private final Locator continueButton;

    public CreditCardFormPage(Config config)
    {
        super(config);
        cardNumberField = page.frameLocator("#snap-midtrans").locator("div.card-number-input-container input[type='tel']");
        expiryField = page.frameLocator("#snap-midtrans").locator("#card-expiry");
        cvvField = page.frameLocator("#snap-midtrans").locator("#card-cvv");
        amountDisplay = page.frameLocator("#snap-midtrans").locator("div.header-amount");
        continueButton = page.frameLocator("#snap-midtrans").locator("button.btn.full.primary.btn-theme");
        assertPageLoaded(cardNumberField);
    }

    public CreditCardFormPage fillCardDetails(PaymentGatewayData payment)
    {
        fillText(cardNumberField, payment.getCardNumber(), "Card number field");
        fillText(expiryField, payment.getExpiry(), "Expiry field");
        fillText(cvvField, payment.getCvv(), "CVV field");
        return this;
    }

    public CreditCardFormPage applyPromo(Promo promo)
    {
        Locator promoOption = page.frameLocator("#snap-midtrans").locator("label[for='" + promo.getKey() + "']");
        click(promoOption, promo.getLabel());
        // Applying the promo recalculates the discounted amount via an async call; wait
        // for that to settle before the caller reads the header amount, or it reads the
        // pre-promo value still displayed at click time.
        WaitHelper.waitForNetworkIdle(config);
        return this;
    }

    public String getAmount()
    {
        return normalizeAmount(getText(amountDisplay, "Amount display"));
    }

    public IssuingBankPage continueToBank()
    {
        click(continueButton, "Continue button");
        WaitHelper.waitForNetworkIdle(config);
        return new IssuingBankPage(config);
    }

    /**
     * Reduces a displayed amount ('Rp50.000', '49000.00') to plain number text
     * ('50000', '49000') - no currency symbol, no thousands separator, no trailing
     * decimal zeros - so two differently-formatted amounts can be compared as strings.
     *
     * Once a promo is applied, the header shows the discounted amount immediately
     * followed by the struck-through original amount (e.g. 'Rp49.000Rp50.000'), and
     * textContent() returns both concatenated - so only the first amount token in the
     * raw text is taken; the original (struck-through) amount that may follow is ignored.
     */
    private String normalizeAmount(String rawAmount)
    {
        Matcher firstAmountMatcher = Pattern.compile("[0-9][0-9.,]*").matcher(rawAmount);
        String firstAmount = firstAmountMatcher.find() ? firstAmountMatcher.group() : rawAmount;
        String digitsAndDot = firstAmount.replaceAll("[^0-9.]", "");
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
