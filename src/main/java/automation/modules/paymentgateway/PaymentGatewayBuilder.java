package automation.modules.paymentgateway;

import automation.core.DataGenerator;

/**
 * Fluent builder for PaymentGatewayData with sensible defaults.
 */
public class PaymentGatewayBuilder
{
    private String amount;
    private String customerName;
    private String email;
    private String address;
    private String phone;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String bankOtp;

    public PaymentGatewayBuilder withAmount(String amount)
    {
        this.amount = amount;
        return this;
    }

    public PaymentGatewayBuilder withCustomerName(String customerName)
    {
        this.customerName = customerName;
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

    public PaymentGatewayBuilder withExpiryDate(String expiryDate)
    {
        this.expiryDate = expiryDate;
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
        if (amount == null) amount = "50000";
        if (customerName == null) customerName = "Mukesh Rajput";
        if (email == null) email = "mrajput@yopmail.com";
        if (address == null) address = "Bangalore, India";
        if (phone == null) phone = DataGenerator.randomPhoneNumber();
        if (cardNumber == null) cardNumber = "4111 1111 1111 1111";
        if (expiryDate == null) expiryDate = "01/35";
        if (cvv == null) cvv = "123";
        if (bankOtp == null) bankOtp = "112233";
        return this;
    }

    public PaymentGatewayData build()
    {
        withDefaults();
        PaymentGatewayData payment = new PaymentGatewayData();
        payment.setAmount(amount);
        payment.setCustomerName(customerName);
        payment.setEmail(email);
        payment.setAddress(address);
        payment.setPhone(phone);
        payment.setCardNumber(cardNumber);
        payment.setExpiryDate(expiryDate);
        payment.setCvv(cvv);
        payment.setBankOtp(bankOtp);
        return payment;
    }
}
