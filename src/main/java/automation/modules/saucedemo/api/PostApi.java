package automation.modules.saucedemo.api;

import automation.core.api.ApiDetails;
import automation.core.api.PathBuilder;

/**
 * JSONPlaceholder REST API endpoints used by SauceDemo API tests.
 * Base URL: https://jsonplaceholder.typicode.com
 *
 * Usage:
 *   api.execute(PostApi.GetPost.withPath("id", "1"), PostData.class);
 *   api.execute(PostApi.CreatePost, post, PostData.class);
 *   api.execute(PostApi.DeletePost.withPath("id", "1"));
 */
public enum PostApi implements ApiDetails
{
    ListPosts( Method.GET,    "/posts",      200),
    GetPost(   Method.GET,    "/posts/{id}", 200),
    CreatePost(Method.POST,   "/posts",      201),
    UpdatePost(Method.PUT,    "/posts/{id}", 200),
    PatchPost( Method.PATCH,  "/posts/{id}", 200),
    DeletePost(Method.DELETE, "/posts/{id}", 200);

    private final Method method;
    private final String endpoint;
    private final int expectedStatus;

    PostApi(Method method, String endpoint, int expectedStatus)
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
