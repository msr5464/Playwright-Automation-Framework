package automation.modules.paymentgateway;

import automation.core.DataGenerator;

/**
 * Fluent builder for PaymentGatewayData with sensible defaults.
 * Field shapes (word count, character kinds) mirror the values validated on the
 * live Midtrans demo checkout flow - do not change the shape when randomising.
 */
public class PaymentGatewayBuilder
{
    private String amount;
    private String name;
    private String email;
    private String address;
    private String phone;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;
    private String promoCode;
    private String bankOtp;

    public PaymentGatewayBuilder withAmount(String amount)
    {
        this.amount = amount;
        return this;
    }

    public PaymentGatewayBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public PaymentGatewayBuilder withEmail(String email)
    {
        this.email = email;
        return this;
    }

    public PaymentGatewayBuilder withAddress(String address)
    {
        this.address = address;
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
        if (amount == null) amount = "50000";
        if (name == null) name = DataGenerator.randomAlphaString(6) + " " + DataGenerator.randomAlphaString(6);
        if (email == null) email = DataGenerator.randomEmail();
        if (address == null) address = "Jl. " + DataGenerator.randomAlphaString(6) + " No. " + DataGenerator.randomNumber(1, 99);
        if (phone == null) phone = "08" + DataGenerator.randomNumber(100000000, 999999999);
        if (cardNumber == null) cardNumber = "4811111111111114";
        if (cardExpiry == null) cardExpiry = "12/29";
        if (cardCvv == null) cardCvv = "123";
        if (promoCode == null) promoCode = "Flash Sale (Credit-Card)";
        if (bankOtp == null) bankOtp = "112233";
        return this;
    }

    public PaymentGatewayData build()
    {
        withDefaults();
        PaymentGatewayData data = new PaymentGatewayData();
        data.setAmount(amount);
        data.setName(name);
        data.setEmail(email);
        data.setAddress(address);
        data.setPhone(phone);
        data.setCardNumber(cardNumber);
        data.setCardExpiry(cardExpiry);
        data.setCardCvv(cardCvv);
        data.setPromoCode(promoCode);
        data.setBankOtp(bankOtp);
        return data;
    }
}
