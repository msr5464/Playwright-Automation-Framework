package automation.modules.paymentpage;

import automation.core.DataGenerator;

public class PaymentPageBuilder
{
    private String name;
    private String phone;
    private String amount;

    public PaymentPageBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public PaymentPageBuilder withPhone(String phone)
    {
        this.phone = phone;
        return this;
    }

    public PaymentPageBuilder withAmount(String amount)
    {
        this.amount = amount;
        return this;
    }

    public PaymentPageBuilder withDefaults()
    {
        if (name == null) name = DataGenerator.randomFullName();
        if (phone == null) phone = "08" + DataGenerator.randomNumber(100000000, 999999999);
        if (amount == null) amount = "19000";
        return this;
    }

    public PaymentPageData build()
    {
        withDefaults();
        PaymentPageData data = new PaymentPageData();
        data.setName(name);
        data.setPhone(phone);
        data.setAmount(amount);
        return data;
    }
}
