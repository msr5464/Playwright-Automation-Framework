package automation.modules.paymentgateway.api;

import automation.core.api.ApiDetails;
import automation.core.api.PathBuilder;

/**
 * Midtrans payment gateway has no direct REST endpoints consumed by these tests.
 * Placeholder enum kept so the module compiles as a complete module structure.
 */
public enum PaymentGatewayApi implements ApiDetails
{
    // No API endpoints — this module uses web-only flows
    ;

    @Override public Method getMethod()      { return null; }
    @Override public String getEndpoint()    { return null; }
    @Override public int getExpectedStatus() { return 0; }

    public PathBuilder withPath(String param, String value)
    {
        return new PathBuilder(this.getMethod(), this.getEndpoint(), this.getExpectedStatus()).withPath(param, value);
    }
}
