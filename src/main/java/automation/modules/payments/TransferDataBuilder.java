package automation.modules.payments;

import automation.core.DataGenerator;

public class TransferDataBuilder
{

    private String amount;
    private String currency = "SGD";
    private String sourceCurrency = "SGD";
    private String destinationCurrency = "SGD";
    private String purpose = "Business payment";
    private String category = "Business";
    private String recipientName;
    private String transferType = "Local";

    public TransferDataBuilder withAmount(String amount)
    {
        this.amount = amount;
        return this;
    }

    public TransferDataBuilder withCurrency(String currency)
    {
        this.currency = currency;
        this.sourceCurrency = currency;
        this.destinationCurrency = currency;
        return this;
    }

    public TransferDataBuilder withSourceCurrency(String currency)
    {
        this.sourceCurrency = currency;
        return this;
    }

    public TransferDataBuilder withDestinationCurrency(String currency)
    {
        this.destinationCurrency = currency;
        return this;
    }

    public TransferDataBuilder withPurpose(String purpose)
    {
        this.purpose = purpose;
        return this;
    }

    public TransferDataBuilder withCategory(String category)
    {
        this.category = category;
        return this;
    }

    public TransferDataBuilder withRecipientName(String name)
    {
        this.recipientName = name;
        return this;
    }

    public TransferDataBuilder withTransferType(String type)
    {
        this.transferType = type;
        return this;
    }

    public TransferDataBuilder withDefaults()
    {
        if (this.amount == null) this.amount = String.valueOf(DataGenerator.randomNumber(10, 100));
        if (this.recipientName == null) this.recipientName = DataGenerator.randomFullName();
        return this;
    }

    public TransferData build()
    {
        withDefaults();
        TransferData transfer = new TransferData();
        transfer.setAmount(amount);
        transfer.setCurrency(currency);
        transfer.setSourceCurrency(sourceCurrency);
        transfer.setDestinationCurrency(destinationCurrency);
        transfer.setPurpose(purpose);
        transfer.setCategory(category);
        transfer.setRecipientName(recipientName);
        transfer.setTransferType(transferType);
        return transfer;
    }
}
