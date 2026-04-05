package automation.core.api;

/**
 * Supports chaining multiple withPath() substitutions for enum-based API endpoints.
 *
 * Usage:
 *   ApiDetails endpoint = CardApi.GetCard.withPath("id", "card-123");
 *   ApiDetails endpoint = GitHubApi.GetRepository
 *       .withPath("owner", "microsoft")
 *       .withPath("repo", "vscode");
 */
public class PathBuilder implements ApiDetails
{
    private final ApiDetails.Method method;
    private final int expectedStatus;
    private String endpoint;

    public PathBuilder(ApiDetails.Method method, String endpoint, int expectedStatus)
    {
        this.method = method;
        this.endpoint = endpoint;
        this.expectedStatus = expectedStatus;
    }

    public PathBuilder withPath(String param, String value)
    {
        String placeholder = "{" + param + "}";
        if (!this.endpoint.contains(placeholder))
        {
            throw new IllegalArgumentException(
                "Unknown path parameter '" + param + "' in endpoint: " + this.endpoint);
        }
        this.endpoint = this.endpoint.replace(placeholder, value);
        return this;
    }

    @Override public ApiDetails.Method getMethod() { return method; }
    @Override public String getEndpoint() { return endpoint; }
    @Override public int getExpectedStatus() { return expectedStatus; }
}
