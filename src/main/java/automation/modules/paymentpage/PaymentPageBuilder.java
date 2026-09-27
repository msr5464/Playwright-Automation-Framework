package automation.modules.paymentpage;

import automation.core.DataGenerator;

/**
 * Fluent builder for PaymentPageData with sensible defaults for card and promo fields.
 */
public class PaymentPageBuilder
{
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String cardNumber;
    private String expiry;
    private String cvv;
    private String promoCode;

    public PaymentPageBuilder withFirstName(String firstName)   { this.firstName  = firstName;  return this; }
    public PaymentPageBuilder withLastName(String lastName)     { this.lastName   = lastName;   return this; }
    public PaymentPageBuilder withEmail(String email)           { this.email      = email;      return this; }
    public PaymentPageBuilder withPhone(String phone)           { this.phone      = phone;      return this; }
    public PaymentPageBuilder withCardNumber(String cardNumber) { this.cardNumber = cardNumber; return this; }
    public PaymentPageBuilder withExpiry(String expiry)         { this.expiry     = expiry;     return this; }
    public PaymentPageBuilder withCvv(String cvv)               { this.cvv        = cvv;        return this; }
    public PaymentPageBuilder withPromoCode(String promoCode)   { this.promoCode  = promoCode;  return this; }

    public PaymentPageBuilder withDefaults()
    {
        if (firstName  == null) firstName  = DataGenerator.randomAlphaString(6);
        if (lastName   == null) lastName   = DataGenerator.randomAlphaString(6);
        if (email      == null) email      = DataGenerator.randomEmail();
        if (phone      == null) phone      = "08" + DataGenerator.randomNumber(100000000, 999999999);
        if (cardNumber == null) cardNumber = "4111111111111111";
        if (expiry     == null) expiry     = "01/35";
        if (cvv        == null) cvv        = "123";
        if (promoCode  == null) promoCode  = "Promo Flash Sale (Credit-Card)";
        return this;
    }

    public PaymentPageData build()
    {
        withDefaults();
        PaymentPageData data = new PaymentPageData();
        data.setFirstName(firstName);
        data.setLastName(lastName);
        data.setEmail(email);
        data.setPhone(phone);
        data.setCardNumber(cardNumber);
        data.setExpiry(expiry);
        data.setCvv(cvv);
        data.setPromoCode(promoCode);
        return data;
    }
}
