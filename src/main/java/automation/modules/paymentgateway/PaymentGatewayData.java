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
    @JsonProperty("name")
    private String name;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("card_expiry")
    private String cardExpiry;

    @JsonProperty("card_cvv")
    private String cardCvv;

    @JsonProperty("promo_code")
    private String promoCode;

    @JsonProperty("otp")
    private String otp;

    @JsonProperty("amount_after_promo")
    private String amountAfterPromo;

    @JsonProperty("order_id")
    private String orderId;
}
