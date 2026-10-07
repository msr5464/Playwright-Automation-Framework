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
    private String cardExpiry;
    private String cardCvv;
    private String promoCode;
    private String bankOtp;
    private String orderId;

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

    public PaymentGatewayBuilder withOrderId(String orderId)
    {
        this.orderId = orderId;
        return this;
    }

    public PaymentGatewayBuilder withDefaults()
    {
        if (phone == null) phone = DataGenerator.randomPhoneNumber();
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
        payment.setCardExpiry(cardExpiry);
        payment.setCardCvv(cardCvv);
        payment.setPromoCode(promoCode);
        payment.setBankOtp(bankOtp);
        payment.setOrderId(orderId);
        return payment;
    }
}
