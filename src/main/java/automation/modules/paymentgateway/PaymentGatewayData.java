package automation.modules.paymentgateway;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentGatewayData
{
    @JsonProperty("amount")
    private String amount;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("address")
    private String address;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("card_expiry")
    private String cardExpiry;

    @JsonProperty("card_cvv")
    private String cardCvv;

    @JsonProperty("promo_code")
    private String promoCode;

    @JsonProperty("bank_otp")
    private String bankOtp;

    @JsonProperty("order_id")
    private String orderId;
}
