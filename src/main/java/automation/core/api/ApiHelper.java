package automation.core.api;

import io.restassured.response.Response;

import automation.core.Config;
import automation.core.AssertHelper;
import automation.core.Log;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single entry point for all API operations in tests.
 *
 * Usage:
 *   ApiHelper api = new ApiHelper(config);
 *   api.loginAndSetAuth(user);
 *   CardData created = api.execute(CardApi.CreateCard, card, CardData.class);
 *   api.execute(CardApi.DeleteCard.withPath("id", created.getId()));
 */
public class ApiHelper extends BaseApiClient
{

    public ApiHelper(Config config)
    {
        super(config);
    }

    public ApiHelper(Config config, String customBaseUrl)
    {
        super(config, customBaseUrl);
    }

    // ========== EXECUTE (one-liner API calls) ==========

    /**
     * Execute API, assert expected status, return typed POJO.
     *
     *   CardData created = api.execute(CardApi.CreateCard, card, CardData.class);
     */
    public <T> T execute(ApiDetails apiDetails, Object body, Class<T> responseType)
    {
        Response response = executeRaw(apiDetails, body);
        assertStatus(response, apiDetails.getExpectedStatus());
        return response.as(responseType);
    }

    /**
     * Execute API with body, assert expected status, return raw Response.
     */
    public Response execute(ApiDetails apiDetails, Object body)
    {
        Response response = executeRaw(apiDetails, body);
        assertStatus(response, apiDetails.getExpectedStatus());
        return response;
    }

    /**
     * Execute API without body, assert expected status, return typed POJO.
     *
     *   GitHubData user = api.execute(GitHubApi.GetUser.withPath("username", "octocat"), GitHubData.class);
     *   CardData card   = api.execute(CardApi.GetCard.withPath("id", id), CardData.class);
     */
    public <T> T execute(ApiDetails apiDetails, Class<T> responseType)
    {
        return execute(apiDetails, null, responseType);
    }

    /**
     * Execute API without body, assert expected status, return raw Response.
     *
     *   api.execute(CardApi.DeleteCard.withPath("id", cardId));
     */
    public Response execute(ApiDetails apiDetails)
    {
        return execute(apiDetails, (Object) null);
    }

    /**
     * Execute API and verify response fields match expected POJO.
     *
     *   api.executeAndVerify(CardApi.GetCard.withPath("id", id), null, expectedCard);
     */
    public <T> T executeAndVerify(ApiDetails apiDetails, Object requestBody, T expectedPojo)
    {
        Response response = executeRaw(apiDetails, requestBody);
        assertStatus(response, apiDetails.getExpectedStatus());

        @SuppressWarnings("unchecked")
        Class<T> responseType = (Class<T>) expectedPojo.getClass();
        T actual = response.as(responseType);

        var mapper = getObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> expectedMap = mapper.convertValue(expectedPojo, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> actualMap = mapper.convertValue(actual, Map.class);

        for (Map.Entry<String, Object> entry : expectedMap.entrySet())
        {
            if (entry.getValue() != null)
            {
                Object actualVal = actualMap.get(entry.getKey());
                AssertHelper.assertEquals(config,
                    String.valueOf(actualVal),
                    String.valueOf(entry.getValue()),
                    "Field '" + entry.getKey() + "'");
            }
        }
        return actual;
    }

    /**
     * Execute API without any status assertion. Use for negative tests.
     *
     *   Response response = api.executeRaw(CardApi.CreateCard, invalidData);
     *   AssertHelper.assertEquals(config, response.getStatusCode(), 400, "Should fail");
     */
    public Response executeRaw(ApiDetails apiDetails, Object body)
    {
        String endpoint = apiDetails.getEndpoint();
        String fullUrl = baseUrl + endpoint;
        Log.comment(config, "API " + apiDetails.getMethod() + " " + fullUrl);

        return switch (apiDetails.getMethod())
        {
            case GET -> get(endpoint);
            case POST -> post(endpoint, body);
            case PUT -> put(endpoint, body);
            case PATCH -> patch(endpoint, body);
            case DELETE -> delete(endpoint);
        };
    }

    public Response executeRaw(ApiDetails apiDetails, Object body, Map<String, String> extraHeaders)
    {
        String endpoint = apiDetails.getEndpoint();
        String fullUrl = baseUrl + endpoint;
        Log.comment(config, "API " + apiDetails.getMethod() + " " + fullUrl);

        return switch (apiDetails.getMethod())
        {
            case GET -> getWithHeaders(endpoint, extraHeaders);
            case POST -> post(endpoint, body, extraHeaders);
            case PUT -> put(endpoint, body, extraHeaders);
            case PATCH -> patch(endpoint, body, extraHeaders);
            case DELETE -> delete(endpoint, extraHeaders);
        };
    }

    // ========== MAP BUILDER (for edge-case/negative tests) ==========

    /**
     * Build a dynamic request body when you can't use a POJO.
     *
     *   api.execute(TransferApi.Create, api.map().put("amount", "100").build());
     */
    public MapBuilder map()
    {
        return new MapBuilder();
    }

    public static class MapBuilder
    {
        private final Map<String, Object> data = new LinkedHashMap<>();

        public MapBuilder put(String key, Object value) { data.put(key, value); return this; }
        public MapBuilder putIf(boolean condition, String key, Object value) { if (condition) data.put(key, value); return this; }
        public MapBuilder putAll(Map<String, ?> values) { data.putAll(values); return this; }
        public Map<String, Object> build() { return data; }
    }

    // ========== RETRY ==========

    /**
     * Execute with retry for rate-limited endpoints.
     */
    public Response executeWithRetry(ApiDetails apiDetails, Object body, int maxRetries, int delayMs)
    {
        Response lastResponse = null;
        int[] retryStatuses = {429, 503};
        for (int attempt = 1; attempt <= maxRetries + 1; attempt++)
        {
            lastResponse = executeRaw(apiDetails, body);
            boolean shouldRetry = false;
            for (int code : retryStatuses)
            {
                if (lastResponse.getStatusCode() == code) { shouldRetry = true; break; }
            }
            if (!shouldRetry) return lastResponse;
            if (attempt <= maxRetries)
            {
                Log.comment(config, "Retrying " + attempt + "/" + maxRetries + " in " + delayMs + "ms...");
                try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
            }
        }
        return lastResponse;
    }
}
