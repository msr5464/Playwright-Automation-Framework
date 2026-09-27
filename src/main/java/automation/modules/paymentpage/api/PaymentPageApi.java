package automation.modules.paymentpage.api;

import automation.core.api.ApiDetails;
import automation.core.api.PathBuilder;

/**
 * PaymentPage API endpoints.
 * No server-side API is currently under test — this enum is a placeholder
 * for future endpoint additions.
 */
public enum PaymentPageApi implements ApiDetails
{
    // No endpoints defined — paymentpage tests are web-only
    ;

    private final ApiDetails.Method method;
    private final String endpoint;
    private final int expectedStatus;

    PaymentPageApi(ApiDetails.Method method, String endpoint, int expectedStatus)
    {
        this.method = method;
        this.endpoint = endpoint;
        this.expectedStatus = expectedStatus;
    }

    @Override public ApiDetails.Method getMethod()      { return method; }
    @Override public String getEndpoint()    { return endpoint; }
    @Override public int getExpectedStatus() { return expectedStatus; }

    public PathBuilder withPath(String param, String value)
    {
        return new PathBuilder(this.method, this.endpoint, this.expectedStatus).withPath(param, value);
    }
}
