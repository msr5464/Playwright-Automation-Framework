package automation.modules.paymentgateway.api;

import automation.core.api.ApiDetails;
import automation.core.api.PathBuilder;

/**
 * API endpoints for the PaymentGateway module.
 * This module currently exercises the Midtrans demo checkout as a web-only flow,
 * so no API endpoints are defined yet. Add ApiDetails-based entries here when a
 * PaymentGateway API endpoint is confirmed for testing.
 */
public enum PaymentGatewayApi implements ApiDetails
{
    ;

    private final Method method;
    private final String endpoint;
    private final int expectedStatus;

    PaymentGatewayApi(Method method, String endpoint, int expectedStatus)
    {
        this.method = method;
        this.endpoint = endpoint;
        this.expectedStatus = expectedStatus;
    }

    @Override public Method getMethod()      { return method; }
    @Override public String getEndpoint()    { return endpoint; }
    @Override public int getExpectedStatus() { return expectedStatus; }

    public PathBuilder withPath(String param, String value)
    {
        return new PathBuilder(this.method, this.endpoint, this.expectedStatus).withPath(param, value);
    }
}
