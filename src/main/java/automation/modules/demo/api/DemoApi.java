package automation.modules.demo.api;

import automation.core.api.ApiDetails;
import automation.core.api.PathBuilder;

public enum DemoApi implements ApiDetails
{
    GetHome(Method.GET, "/", 200);

    private final Method method;
    private final String endpoint;
    private final int expectedStatus;

    DemoApi(Method method, String endpoint, int expectedStatus)
    {
        this.method = method;
        this.endpoint = endpoint;
        this.expectedStatus = expectedStatus;
    }

    @Override public Method getMethod() { return method; }
    @Override public String getEndpoint() { return endpoint; }
    @Override public int getExpectedStatus() { return expectedStatus; }

    public PathBuilder withPath(String param, String value)
    {
        return new PathBuilder(this.method, this.endpoint, this.expectedStatus).withPath(param, value);
    }
}
