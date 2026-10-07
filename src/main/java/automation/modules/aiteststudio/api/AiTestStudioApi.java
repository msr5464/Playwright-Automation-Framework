package automation.modules.aiteststudio.api;

import automation.core.api.ApiDetails;

/**
 * AI-Test-Studio REST API endpoints. Base URL: {@code aiteststudio.api.url}.
 */
public enum AiTestStudioApi implements ApiDetails
{
    Login(Method.POST, "/api/auth/login", 200),
    Query(Method.POST, "/api/customer/query", 200);

    private final Method method;
    private final String endpoint;
    private final int expectedStatus;

    AiTestStudioApi(Method method, String endpoint, int expectedStatus)
    {
        this.method = method;
        this.endpoint = endpoint;
        this.expectedStatus = expectedStatus;
    }

    @Override public Method getMethod() { return method; }
    @Override public String getEndpoint() { return endpoint; }
    @Override public int getExpectedStatus() { return expectedStatus; }
}
