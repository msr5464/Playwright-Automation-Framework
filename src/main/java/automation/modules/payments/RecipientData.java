package automation.modules.payments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecipientData
{

    private String name;

    private String email;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("routing_code")
    private String routingCode;

    @JsonProperty("swift_code")
    private String swiftCode;

    private String country;

    private String currency;

    @JsonProperty("recipient_type")
    private String recipientType;

    private String address;

    private String id;
}
