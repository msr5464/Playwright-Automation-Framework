package automation.core;

import automation.core.Enums.UserType;
import automation.core.Enums.Feature;
import automation.core.Enums.Country;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User
{
    private int id;
    private String username;
    private String password;
    private String otp;
    private String otpSecret;
    private String businessUuid;
    private String personUuid;
    private UserType userType;
    private Feature feature;
    private Country country;
    private String usageStatus;
    private String testcaseName;
    private boolean isPoolUser;
}
