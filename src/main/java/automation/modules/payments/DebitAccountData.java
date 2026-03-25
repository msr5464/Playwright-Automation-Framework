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
public class DebitAccountData
{

    @JsonProperty("account_uuid")
    private String accountUuid;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("account_number")
    private String accountNumber;

    private String currency;
    private String balance;

    @JsonProperty("account_type")
    private String accountType;
}
