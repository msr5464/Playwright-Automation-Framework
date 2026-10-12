package automation.modules.toolshop;

/**
 * Fluent builder for ToolshopData with sensible defaults.
 *
 * The checkout fields below are the test case's own data (step 02 validated them
 * against the live page) and are therefore fixed defaults here, never randomised:
 * country, postalCode, houseNumber, cardNumber, expirationDate, cvv, cardHolderName
 * and productName.
 */
public class ToolshopBuilder
{
    private String productName;
    private String quantity;
    private String country;
    private String postalCode;
    private String houseNumber;
    private String cardNumber;
    private String expirationDate;
    private String cvv;
    private String cardHolderName;

    public ToolshopBuilder withProductName(String productName)
    {
        this.productName = productName;
        return this;
    }

    public ToolshopBuilder withQuantity(String quantity)
    {
        this.quantity = quantity;
        return this;
    }

    public ToolshopBuilder withCountry(String country)
    {
        this.country = country;
        return this;
    }

    public ToolshopBuilder withPostalCode(String postalCode)
    {
        this.postalCode = postalCode;
        return this;
    }

    public ToolshopBuilder withHouseNumber(String houseNumber)
    {
        this.houseNumber = houseNumber;
        return this;
    }

    public ToolshopBuilder withCardNumber(String cardNumber)
    {
        this.cardNumber = cardNumber;
        return this;
    }

    public ToolshopBuilder withExpirationDate(String expirationDate)
    {
        this.expirationDate = expirationDate;
        return this;
    }

    public ToolshopBuilder withCvv(String cvv)
    {
        this.cvv = cvv;
        return this;
    }

    public ToolshopBuilder withCardHolderName(String cardHolderName)
    {
        this.cardHolderName = cardHolderName;
        return this;
    }

    public ToolshopBuilder withDefaults()
    {
        if (productName == null) productName = "Combination Pliers";
        if (quantity == null) quantity = "2";
        if (country == null) country = "Germany";
        if (postalCode == null) postalCode = "60311";
        if (houseNumber == null) houseNumber = "12";
        if (cardNumber == null) cardNumber = "1111-2222-3333-4444";
        if (expirationDate == null) expirationDate = "12/2030";
        if (cvv == null) cvv = "123";
        if (cardHolderName == null) cardHolderName = "Jack Howe";
        return this;
    }

    public ToolshopData build()
    {
        withDefaults();
        ToolshopData order = new ToolshopData();
        order.setProductName(productName);
        order.setQuantity(quantity);
        order.setCountry(country);
        order.setPostalCode(postalCode);
        order.setHouseNumber(houseNumber);
        order.setCardNumber(cardNumber);
        order.setExpirationDate(expirationDate);
        order.setCvv(cvv);
        order.setCardHolderName(cardHolderName);
        return order;
    }
}
