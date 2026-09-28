package automation.modules.paymentgateway;

import automation.core.DataGenerator;

/**
 * Fluent builder for PaymentGatewayData with sensible defaults.
 */
public class PaymentGatewayBuilder
{
    private String name;
    private String phone;
    private String amount;
    private String cardNumber = "4111 1111 1111 1111";
    private String cardExpiry = "01/35";
    private String cardCvv = "123";
    private String promoCode = "Promo Flash Sale (Credit-Card)";
    private String otp = "112233";

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

    public PaymentGatewayBuilder withOtp(String otp)
    {
        this.otp = otp;
        return this;
    }

    public PaymentGatewayBuilder withDefaults()
    {
        if (name == null) name = DataGenerator.randomAlphaString(6) + " " + DataGenerator.randomAlphaString(6);
        if (phone == null) phone = "0" + DataGenerator.randomNumber(100000000, 999999999);
        if (amount == null) amount = String.valueOf(DataGenerator.randomNumber(10000, 99999));
        return this;
    }

    public PaymentGatewayData build()
    {
        withDefaults();
        PaymentGatewayData data = new PaymentGatewayData();
        data.setName(name);
        data.setPhone(phone);
        data.setAmount(amount);
        data.setCardNumber(cardNumber);
        data.setCardExpiry(cardExpiry);
        data.setCardCvv(cardCvv);
        data.setPromoCode(promoCode);
        data.setOtp(otp);
        return data;
    }
}
