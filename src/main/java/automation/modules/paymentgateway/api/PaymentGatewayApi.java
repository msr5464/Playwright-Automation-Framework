package automation.modules.paymentgateway.api;

import automation.core.api.ApiDetails;
import automation.core.api.PathBuilder;

/**
 * PaymentGateway API endpoints.
 * This module is web-only; no REST endpoints are defined.
 */
public enum PaymentGatewayApi implements ApiDetails
{
    // No API endpoints — this module is web-only (Midtrans demo site).
    ;

    @Override public ApiDetails.Method getMethod()      { return null; }
    @Override public String getEndpoint()               { return null; }
    @Override public int getExpectedStatus()            { return 0; }

    public PathBuilder withPath(String param, String value)
    {
        return new PathBuilder(getMethod(), getEndpoint(), getExpectedStatus()).withPath(param, value);
    }
}
