package automation.modules.paymentgateway;

import automation.core.DataGenerator;

/**
 * Fluent builder for PaymentGatewayData with sensible defaults.
 * Defaults for amount, name, email, address, cardNumber, expiry, cvv and otp are the
 * exact values the test case specifies — never randomised.
 */
public class PaymentGatewayBuilder
{
    private String amount;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String cardNumber;
    private String expiry;
    private String cvv;
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
        if (amount == null) amount = "50000";
        if (name == null) name = "Mukesh Rajput";
        if (email == null) email = "mrajput@yopmail.com";
        if (phone == null) phone = DataGenerator.randomPhoneNumber();
        if (address == null) address = "Bangalore, India";
        if (cardNumber == null) cardNumber = "4111 1111 1111 1111";
        if (expiry == null) expiry = "01/35";
        if (cvv == null) cvv = "123";
        if (otp == null) otp = "112233";
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
