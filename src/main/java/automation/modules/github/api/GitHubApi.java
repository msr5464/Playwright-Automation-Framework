package automation.modules.github.api;

import automation.core.api.ApiDetails;
import automation.core.api.PathBuilder;

/**
 * GitHub REST API endpoints.
 *
 * Usage - single parameter:
 *   api.execute(GitHubApi.GetUser.withPath("username", "octocat"), GitHubData.class);
 *
 * Usage - multiple parameters (chainable):
 *   api.execute(GitHubApi.GetRepository
 *       .withPath("owner", "microsoft")
 *       .withPath("repo", "vscode"), GitHubData.class);
 */
public enum GitHubApi implements ApiDetails
{
    // User endpoints
    GetAuthenticatedUser(Method.GET, "/user", 200),
    GetUser(Method.GET, "/users/{username}", 200),
    UpdateUser(Method.PATCH, "/user", 200),

    // Repository endpoints
    GetRepository(Method.GET, "/repos/{owner}/{repo}", 200),
    CreateRepository(Method.POST, "/user/repos", 201),
    ListUserRepos(Method.GET, "/users/{username}/repos", 200),
    ListAuthenticatedUserRepos(Method.GET, "/user/repos", 200),
    UpdateRepository(Method.PATCH, "/repos/{owner}/{repo}", 200),
    DeleteRepository(Method.DELETE, "/repos/{owner}/{repo}", 204),

    // Issues endpoints
    ListRepositoryIssues(Method.GET, "/repos/{owner}/{repo}/issues", 200),
    CreateIssue(Method.POST, "/repos/{owner}/{repo}/issues", 201),
    GetIssue(Method.GET, "/repos/{owner}/{repo}/issues/{issue_number}", 200),
    UpdateIssue(Method.PATCH, "/repos/{owner}/{repo}/issues/{issue_number}", 200),

    // Stars/Watchers endpoints
    GetRepositoryStargazers(Method.GET, "/repos/{owner}/{repo}/stargazers", 200),
    FollowUser(Method.PUT, "/user/following/{username}", 204),
    UnfollowUser(Method.DELETE, "/user/following/{username}", 204),

    // Branch and contributor endpoints
    GetRepositoryBranches(Method.GET, "/repos/{owner}/{repo}/branches", 200),
    GetRepositoryContributors(Method.GET, "/repos/{owner}/{repo}/contributors", 200);

    private final Method method;
    private final String endpoint;
    private final int expectedStatus;

    GitHubApi(Method method, String endpoint, int expectedStatus)
    {
        this.method = method;
        this.endpoint = endpoint;
        this.expectedStatus = expectedStatus;
    }

    @Override public Method getMethod() { return method; }
    @Override public String getEndpoint() { return endpoint; }
    @Override public int getExpectedStatus() { return expectedStatus; }

    /**
     * Replace path parameters with chainable support.
     * GitHubApi.GetRepository.withPath("owner", "microsoft").withPath("repo", "vscode")
     * Returns a PathBuilder that supports chaining.
     */
    public PathBuilder withPath(String param, String value)
    {
        return new PathBuilder(this.method, this.endpoint, this.expectedStatus).withPath(param, value);
    }
}
