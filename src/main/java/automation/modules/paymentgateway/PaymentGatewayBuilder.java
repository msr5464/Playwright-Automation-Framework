package automation.modules.paymentgateway;

import automation.core.DataGenerator;

/**
 * Fluent builder for PaymentGatewayData with sensible defaults.
 * Card defaults match the Midtrans demo accepted test cards.
 * Phone shape: '08' + 9 digits (Indonesian mobile trunk format).
 * Name shape: two words (first name + last name).
 */
public class PaymentGatewayBuilder
{
    private String name;
    private String phone;
    private String cardNumber = "4111111111111111";
    private String expiry     = "01/35";
    private String cvv        = "123";
    private String bankOtp    = "112233";

    public PaymentGatewayBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public PaymentGatewayBuilder withPhone(String phone)
    {
        this.phone = phone;
        return this;
    }

    public PaymentGatewayBuilder withCardNumber(String cardNumber)
    {
        this.cardNumber = cardNumber;
        return this;
    }

    public PaymentGatewayBuilder withExpiry(String expiry)
    {
        this.expiry = expiry;
        return this;
    }

    public PaymentGatewayBuilder withCvv(String cvv)
    {
        this.cvv = cvv;
        return this;
    }

    public PaymentGatewayBuilder withBankOtp(String bankOtp)
    {
        this.bankOtp = bankOtp;
        return this;
    }

    public PaymentGatewayBuilder withDefaults()
    {
        if (name == null)  name  = DataGenerator.randomFullName();
        if (phone == null) phone = "08" + DataGenerator.randomNumber(100000000, 999999999);
        return this;
    }

    public PaymentGatewayData build()
    {
        withDefaults();
        PaymentGatewayData data = new PaymentGatewayData();
        data.setName(name);
        data.setPhone(phone);
        data.setCardNumber(cardNumber);
        data.setExpiry(expiry);
        data.setCvv(cvv);
        data.setBankOtp(bankOtp);
        return data;
    }
}
