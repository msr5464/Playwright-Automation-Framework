package automation.modules.paymentpage.api;

import automation.core.api.ApiDetails;
import automation.core.api.PathBuilder;

/**
 * PaymentPage API endpoints placeholder.
 * This module is web-only; no REST endpoints are defined.
 */
public enum PaymentPageApi implements ApiDetails
{
    // No API endpoints — web-only module
    ;

    @Override public ApiDetails.Method getMethod()      { return null; }
    @Override public String            getEndpoint()    { return null; }
    @Override public int               getExpectedStatus() { return 0; }

    public PathBuilder withPath(String param, String value)
    {
        return new PathBuilder(getMethod(), getEndpoint(), getExpectedStatus()).withPath(param, value);
    }
}
