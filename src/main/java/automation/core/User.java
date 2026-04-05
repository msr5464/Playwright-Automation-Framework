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
    private String businessName;
    private String personReferenceCode;
    private String businessReferenceCode;
    private String fullName;
    private UserType userType;
    private Feature feature;
    private Country country;
    private boolean isPoolUser;
}
