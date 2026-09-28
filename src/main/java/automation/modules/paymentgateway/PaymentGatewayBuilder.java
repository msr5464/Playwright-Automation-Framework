package automation.modules.paymentgateway;

import automation.core.DataGenerator;

/**
 * Fluent builder for PaymentGatewayData with sensible defaults.
 * Names and phone stay in the shape validated on the live Midtrans demo form:
 * a two-word name (first + last, single-word parts) and a numeric phone string.
 */
public class PaymentGatewayBuilder
{
    private String customerName;
    private String customerPhone;
    private String amount;
    private String cardNumber = "4111 1111 1111 1111";
    private String cardExpiry = "01/35";
    private String cardCvv = "123";
    private String promoCode = "Promo Flash Sale (Credit-Card)";
    private String bankOtp = "112233";

    public PaymentGatewayBuilder withCustomerName(String customerName)
    {
        this.customerName = customerName;
        return this;
    }

    public PaymentGatewayBuilder withCustomerPhone(String customerPhone)
    {
        this.customerPhone = customerPhone;
        return this;
    }

    public PaymentGatewayBuilder withAmount(String amount)
    {
        this.amount = amount;
        return this;
    }

    public PaymentGatewayBuilder withCardNumber(String cardNumber)
    {
        this.cardNumber = cardNumber;
        return this;
    }

    public PaymentGatewayBuilder withCardExpiry(String cardExpiry)
    {
        this.cardExpiry = cardExpiry;
        return this;
    }

    public PaymentGatewayBuilder withCardCvv(String cardCvv)
    {
        this.cardCvv = cardCvv;
        return this;
    }

    public PaymentGatewayBuilder withPromoCode(String promoCode)
    {
        this.promoCode = promoCode;
        return this;
    }

    public PaymentGatewayBuilder withBankOtp(String bankOtp)
    {
        this.bankOtp = bankOtp;
        return this;
    }

    public PaymentGatewayBuilder withDefaults()
    {
        if (customerName == null)
        {
            customerName = DataGenerator.randomAlphaString(6) + " " + DataGenerator.randomAlphaString(6);
        }
        if (customerPhone == null)
        {
            customerPhone = "08" + DataGenerator.randomNumber(100000000, 999999999);
        }
        return this;
    }

    public PaymentGatewayData build()
    {
        withDefaults();
        PaymentGatewayData data = new PaymentGatewayData();
        data.setCustomerName(customerName);
        data.setCustomerPhone(customerPhone);
        data.setAmount(amount);
        data.setCardNumber(cardNumber);
        data.setCardExpiry(cardExpiry);
        data.setCardCvv(cardCvv);
        data.setPromoCode(promoCode);
        data.setBankOtp(bankOtp);
        return data;
    }
}
