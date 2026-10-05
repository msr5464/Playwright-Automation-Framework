package automation.modules.paymentgateway;

import automation.core.DataGenerator;

/**
 * Fluent builder for PaymentGatewayData with sensible defaults.
 */
public class PaymentGatewayBuilder
{
    private String amount = "50000";
    private String name = "Mukesh Rajput";
    private String email = "mrajput@yopmail.com";
    private String phone;
    private String address = "Bangalore, India";
    private String cardNumber = "4111 1111 1111 1111";
    private String cardExpiry = "01/35";
    private String cardCvv = "123";

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

    public PaymentGatewayBuilder withDefaults()
    {
        if (phone == null) phone = String.valueOf(DataGenerator.randomNumber(1000000000, 2147483647));
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
        payment.setCardExpiry(cardExpiry);
        payment.setCardCvv(cardCvv);
        return payment;
    }
}
