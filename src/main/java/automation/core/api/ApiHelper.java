package automation.core.api;

import io.restassured.response.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
                AssertHelper.compareEquals(config,
                    "Field '" + entry.getKey() + "'",
                    String.valueOf(entry.getValue()),
                    String.valueOf(actualVal));
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
        Log.action(config, apiDetails.getMethod() + " " + fullUrl);
        logCurlCommand(apiDetails.getMethod().name(), fullUrl, body);
        if (body != null)
        {
            logRequestBody(body);
        }

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
        Log.action(config, apiDetails.getMethod() + " " + fullUrl);
        logCurlCommand(apiDetails.getMethod().name(), fullUrl, body);
        if (body != null)
        {
            logRequestBody(body);
        }

        return switch (apiDetails.getMethod())
        {
            case GET -> getWithHeaders(endpoint, extraHeaders);
            case POST -> post(endpoint, body, extraHeaders);
            case PUT -> put(endpoint, body, extraHeaders);
            case PATCH -> patch(endpoint, body, extraHeaders);
            case DELETE -> delete(endpoint, extraHeaders);
        };
    }

    // ========== ERROR HANDLING ==========

    /**
     * Checks the response for known server/client error codes and calls logFailToEndExecution
     * if an error is found. Use after executeRaw to skip the test on infrastructure errors.
     */
    public void skipOnServerError(Response response)
    {
        int status = response.getStatusCode();
        String body = response.getBody().asString();
        switch (status)
        {
            case 400 -> config.logFailToEndExecution("Bad Request (400): " + body);
            case 401 -> config.logFailToEndExecution("Unauthorized (401): " + body);
            case 403 -> config.logFailToEndExecution("Forbidden (403): " + body);
            case 404 -> config.logFailToEndExecution("Not Found (404): " + body);
            case 422 -> config.logFailToEndExecution("Unprocessable Entity (422): " + body);
            case 500 -> config.logFailToEndExecution("Internal Server Error (500): " + body);
            case 502 -> config.logFailToEndExecution("Bad Gateway (502): " + body);
            case 503 -> config.logFailToEndExecution("Service Unavailable (503): " + body);
            case 504 -> config.logFailToEndExecution("Gateway Timeout (504): " + body);
            default  -> { /* no error */ }
        }
    }

    // ========== JSON RESPONSE UTILITIES ==========

    /**
     * Extract the value of a top-level key from a JSON response body.
     * Calls logFailToEndExecution if the key is not found.
     */
    public String getValueForKeyFromResponse(Response response, String key)
    {
        try
        {
            JSONObject json = new JSONObject(response.getBody().asString());
            if (json.has(key))
            {
                return String.valueOf(json.get(key));
            }
            config.logFailToEndExecution("Key '" + key + "' not found in response: " + response.getBody().asString());
        }
        catch (Exception e)
        {
            config.logExceptionAndFail("Failed to parse response JSON for key '" + key + "'", e);
        }
        return null;
    }

    /**
     * From a JSONArray, return a random object where object[key] == value.
     * Returns null (and logs a warning) if no match is found.
     */
    public JSONObject getRandomJSONObjectByKeyValue(JSONArray array, String key, String value)
    {
        List<JSONObject> matches = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
        {
            JSONObject obj = array.getJSONObject(i);
            if (obj.has(key) && String.valueOf(obj.get(key)).equals(value))
            {
                matches.add(obj);
            }
        }
        if (matches.isEmpty())
        {
            Log.warning(config, "No JSON object found in array where '" + key + "' == '" + value + "'");
            return null;
        }
        Collections.shuffle(matches);
        return matches.get(0);
    }

    // ========== POLLING ==========

    /**
     * Poll an endpoint until the JSON path matches the expected value, or until
     * maxAttempts (10) is reached.
     *
     * @param waitSeconds delay between retries
     */
    public boolean waitForExpectedResponse(ApiDetails apiDetails, Object body,
                                           String jsonPath, Object expectedValue, int waitSeconds)
    {
        int maxAttempts = 10;
        List<Object> mismatches = new ArrayList<>();
        for (int attempt = 1; attempt <= maxAttempts; attempt++)
        {
            Response response = executeRaw(apiDetails, body);
            Object actual;
            try
            {
                actual = response.jsonPath().get(jsonPath);
            }
            catch (Exception e)
            {
                actual = null;
            }

            if (expectedValue.equals(actual))
            {
                Log.pass(config, "waitForExpectedResponse: '" + jsonPath + "' == '" + expectedValue + "' (attempt " + attempt + ")");
                return true;
            }
            mismatches.add("Attempt " + attempt + ": got '" + actual + "'");
            if (attempt < maxAttempts)
            {
                Log.comment(config, "Waiting " + waitSeconds + "s for '" + jsonPath + "' to become '" + expectedValue + "' (attempt " + attempt + "/" + maxAttempts + ")");
                try { Thread.sleep(waitSeconds * 1000L); } catch (InterruptedException ignored) {}
            }
        }
        Log.warning(config, "waitForExpectedResponse: '" + jsonPath + "' never became '" + expectedValue + "'. Results: " + mismatches);
        return false;
    }

    /** Overload with default waitSeconds=3. */
    public boolean waitForExpectedResponse(ApiDetails apiDetails, Object body,
                                           String jsonPath, Object expectedValue)
    {
        return waitForExpectedResponse(apiDetails, body, jsonPath, expectedValue, 3);
    }

    /**
     * Multi-anchor variant: polls until ALL key/value pairs in the anchors map match.
     */
    public boolean waitForExpectedResponse(ApiDetails apiDetails, Object body,
                                           Map<String, Object> anchors, int waitSeconds)
    {
        int maxAttempts = 10;
        for (int attempt = 1; attempt <= maxAttempts; attempt++)
        {
            Response response = executeRaw(apiDetails, body);
            boolean allMatch = true;
            for (Map.Entry<String, Object> anchor : anchors.entrySet())
            {
                Object actual;
                try { actual = response.jsonPath().get(anchor.getKey()); }
                catch (Exception e) { actual = null; }
                if (!anchor.getValue().equals(actual))
                {
                    allMatch = false;
                    Log.comment(config, "Attempt " + attempt + ": '" + anchor.getKey() + "' = '" + actual + "' (expected '" + anchor.getValue() + "')");
                    break;
                }
            }
            if (allMatch)
            {
                Log.pass(config, "waitForExpectedResponse: all anchors matched (attempt " + attempt + ")");
                return true;
            }
            if (attempt < maxAttempts)
            {
                try { Thread.sleep(waitSeconds * 1000L); } catch (InterruptedException ignored) {}
            }
        }
        Log.warning(config, "waitForExpectedResponse: not all anchors matched after " + maxAttempts + " attempts");
        return false;
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
