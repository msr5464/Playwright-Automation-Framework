package automation.modules.paymentpage;

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
public class PaymentPageData
{
    @JsonProperty("first_name")        private String firstName;
    @JsonProperty("last_name")         private String lastName;
    @JsonProperty("email")             private String email;
    @JsonProperty("phone")             private String phone;
    @JsonProperty("card_number")       private String cardNumber;
    @JsonProperty("expiry")            private String expiry;
    @JsonProperty("cvv")               private String cvv;
    @JsonProperty("promo_code")        private String promoCode;
    @JsonProperty("original_amount")   private String originalAmount;
    @JsonProperty("discounted_amount") private String discountedAmount;
    @JsonProperty("order_id")          private String orderId;
}
