package automation.modules.paymentgateway.api;

import automation.core.api.ApiDetails;
import automation.core.api.PathBuilder;

/**
 * PaymentGateway is a web-only module (Midtrans demo checkout) — there are no
 * API endpoints defined for this feature. This enum exists to satisfy the
 * module's expected package structure but currently has no constants.
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
