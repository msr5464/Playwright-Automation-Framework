package automation.modules.paymentgateway.api;

import automation.core.api.ApiDetails;

/**
 * PaymentGateway (Midtrans demo checkout) is a web-only flow for this module -
 * there are no API endpoints defined in the generation plan. This enum exists
 * to satisfy the module's standard structure but currently has no members.
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
}
