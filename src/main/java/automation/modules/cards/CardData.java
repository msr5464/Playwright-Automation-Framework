package automation.modules.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardData
{

    @JsonProperty("card_name")
    private String cardName;

    @JsonProperty("card_purpose")
    private String cardPurpose;

    @JsonProperty("source_of_funds")
    private String sourceOfFunds;

    @JsonProperty("card_color")
    private String cardColor;

    @JsonProperty("spending_limit")
    private String spendingLimit;

    @JsonProperty("card_type")
    private String cardType;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("expiry_date")
    private String expiryDate;

    private String cvv;

    private String status;

    private String id;
}
