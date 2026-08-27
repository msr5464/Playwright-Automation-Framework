package automation.modules.naukari.api;

import automation.core.api.ApiDetails;
import automation.core.api.PathBuilder;

/**
 * Naukri Profile Summary API endpoints.
 * Placeholder enum — this feature uses web-only flows.
 */
public enum NaukriProfileSummaryApi implements ApiDetails
{
    // No API endpoints defined for this web-only feature
    ;

    private final Method method;
    private final String endpoint;
    private final int expectedStatus;

    NaukriProfileSummaryApi(Method method, String endpoint, int expectedStatus)
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
