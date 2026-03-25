package automation.modules.payments;

import automation.core.DataGenerator;

public class RecipientDataBuilder
{

    private String name;
    private String email;
    private String bankName = "DBS Bank";
    private String accountNumber;
    private String routingCode;
    private String swiftCode;
    private String country = "SG";
    private String currency = "SGD";
    private String recipientType = "Individual";
    private String address;

    public RecipientDataBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public RecipientDataBuilder withEmail(String email)
    {
        this.email = email;
        return this;
    }

    public RecipientDataBuilder withBankName(String bankName)
    {
        this.bankName = bankName;
        return this;
    }

    public RecipientDataBuilder withAccountNumber(String accountNumber)
    {
        this.accountNumber = accountNumber;
        return this;
    }

    public RecipientDataBuilder withCountry(String country)
    {
        this.country = country;
        return this;
    }

    public RecipientDataBuilder withCurrency(String currency)
    {
        this.currency = currency;
        return this;
    }

    public RecipientDataBuilder withRecipientType(String type)
    {
        this.recipientType = type;
        return this;
    }

    public RecipientDataBuilder withDefaults()
    {
        if (this.name == null) this.name = DataGenerator.randomFullName();
        if (this.email == null) this.email = DataGenerator.randomEmail();
        if (this.accountNumber == null) this.accountNumber = String.valueOf(DataGenerator.randomNumber(1000000000, 2000000000));
        return this;
    }

    public RecipientData build()
    {
        withDefaults();
        RecipientData recipient = new RecipientData();
        recipient.setName(name);
        recipient.setEmail(email);
        recipient.setBankName(bankName);
        recipient.setAccountNumber(accountNumber);
        recipient.setRoutingCode(routingCode);
        recipient.setSwiftCode(swiftCode);
        recipient.setCountry(country);
        recipient.setCurrency(currency);
        recipient.setRecipientType(recipientType);
        recipient.setAddress(address);
        return recipient;
    }
}
