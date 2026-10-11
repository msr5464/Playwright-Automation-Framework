package automation.modules.toolshop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolshopData
{
    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("quantity")
    private String quantity;

    @JsonProperty("country")
    private String country;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("house_number")
    private String houseNumber;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("expiration_date")
    private String expirationDate;

    @JsonProperty("cvv")
    private String cvv;

    @JsonProperty("card_holder_name")
    private String cardHolderName;

    @JsonProperty("price")
    private String price;

    @JsonProperty("cart_total")
    private String cartTotal;

    @JsonProperty("street")
    private String street;

    @JsonProperty("city")
    private String city;

    @JsonProperty("invoice_number")
    private String invoiceNumber;
}
