package automation.modules.paymentgateway;

import automation.core.DataGenerator;

/**
 * Fluent builder for PaymentGatewayData with sensible defaults.
 */
public class PaymentGatewayBuilder
{
    private String name;
    private String phone;
    private String cardNumber;
    private String expiry;
    private String cvv;
    private String bankOtp;

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
        if (name == null) name = "User_" + DataGenerator.randomAlphaString(5);
        if (phone == null) phone = "+628" + DataGenerator.randomNumber(100000000, 999999999);
        if (cardNumber == null) cardNumber = "4111111111111111";
        if (expiry == null) expiry = "01/35";
        if (cvv == null) cvv = "123";
        if (bankOtp == null) bankOtp = "112233";
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
