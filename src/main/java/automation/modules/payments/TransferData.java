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
public class TransferData
{

    private String amount;

    private String currency;

    @JsonProperty("source_currency")
    private String sourceCurrency;

    @JsonProperty("destination_currency")
    private String destinationCurrency;

    private String purpose;

    private String category;

    @JsonProperty("recipient_name")
    private String recipientName;

    @JsonProperty("transfer_type")
    private String transferType;

    @JsonProperty("reference_number")
    private String referenceNumber;

    private String status;

    @JsonProperty("fx_rate")
    private String fxRate;

    private String fee;

    private String id;
}
