package automation.modules.paymentgateway;

import automation.core.DataGenerator;

/**
 * Fluent builder for PaymentGatewayData with sensible defaults.
 */
public class PaymentGatewayBuilder
{
    private String amount = "50000";
    private String name;
    private String email;
    private String phone;
    private String address = "Bangalore, India";
    private String cardNumber = "4111 1111 1111 1111";
    private String expiry = "01/35";
    private String cvv = "123";
    private String otp = "112233";

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

    public PaymentGatewayBuilder withOtp(String otp)
    {
        this.otp = otp;
        return this;
    }

    public PaymentGatewayBuilder withDefaults()
    {
        if (name == null) name = DataGenerator.randomFirstName() + " " + DataGenerator.randomLastName();
        if (email == null) email = DataGenerator.randomEmail();
        if (phone == null) phone = "0" + DataGenerator.randomNumber(100000000, 999999999) + DataGenerator.randomNumber(0, 9);
        return this;
    }

    public PaymentGatewayData build()
    {
        withDefaults();
        PaymentGatewayData payment = new PaymentGatewayData();
        payment.setAmount(amount);
        payment.setName(name);
        payment.setEmail(email);
        payment.setPhone(phone);
        payment.setAddress(address);
        payment.setCardNumber(cardNumber);
        payment.setExpiry(expiry);
        payment.setCvv(cvv);
        payment.setOtp(otp);
        return payment;
    }
}
