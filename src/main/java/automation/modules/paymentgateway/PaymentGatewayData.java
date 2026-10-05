package automation.modules.paymentgateway;

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
public class PaymentGatewayData
{
    @JsonProperty("amount")
    private String amount;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("address")
    private String address;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("expiry")
    private String expiry;

    @JsonProperty("cvv")
    private String cvv;

    @JsonProperty("otp")
    private String otp;

    @JsonProperty("order_id")
    private String orderId;
}
