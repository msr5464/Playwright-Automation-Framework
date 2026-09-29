package automation.modules.paymentgateway;

import automation.core.DataGenerator;

/**
 * Fluent builder for PaymentGatewayData with sensible defaults matching the
 * Midtrans demo checkout flow's validated test data shape.
 */
public class PaymentGatewayBuilder
{
    private String amount;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;
    private String promoName;
    private String otp;

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

    public PaymentGatewayBuilder withPhone(String phone)
    {
        this.phone = phone;
        return this;
    }

    public PaymentGatewayBuilder withAddress(String address)
    {
        this.address = address;
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

    public PaymentGatewayBuilder withPromoName(String promoName)
    {
        this.promoName = promoName;
        return this;
    }

    public PaymentGatewayBuilder withOtp(String otp)
    {
        this.otp = otp;
        return this;
    }

    public PaymentGatewayBuilder withDefaults()
    {
        if (amount == null) amount = "50000";
        if (name == null) name = "Mukesh Rajput";
        if (email == null) email = "mrajput@yopmail.com";
        if (phone == null) phone = "9" + DataGenerator.randomNumber(100000000, 999999999);
        if (address == null) address = "Bangalore, India";
        if (cardNumber == null) cardNumber = "4111 1111 1111 1111";
        if (cardExpiry == null) cardExpiry = "01/35";
        if (cardCvv == null) cardCvv = "123";
        if (promoName == null) promoName = "Promo Flash Sale (Credit-Card)";
        if (otp == null) otp = "112233";
        return this;
    }

    public PaymentGatewayData build()
    {
        withDefaults();
        PaymentGatewayData data = new PaymentGatewayData();
        data.setAmount(amount);
        data.setName(name);
        data.setEmail(email);
        data.setPhone(phone);
        data.setAddress(address);
        data.setCardNumber(cardNumber);
        data.setCardExpiry(cardExpiry);
        data.setCardCvv(cardCvv);
        data.setPromoName(promoName);
        data.setOtp(otp);
        return data;
    }
}
