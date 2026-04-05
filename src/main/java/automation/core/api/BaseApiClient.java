package automation.core.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import automation.core.Config;
import automation.core.AssertHelper;
import automation.core.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Low-level REST Assured client with auth sharing.
 * Tests should NOT use this directly — use {@link automation.core.api.ApiHelper} instead.
 */
public class BaseApiClient
{

    // Shared auth state across API client instances within the same test (keyed by testId)
    private static final Map<String, AuthState> AUTH_STATES = new ConcurrentHashMap<>();

    private static class AuthState
    {
        String token;
        String businessUuid;
        String personUuid;
        String debitAccountUuid;
        final Map<String, String> customHeaders = new ConcurrentHashMap<>();
    }

    private static AuthState getOrCreateAuth(String testId)
    {
        return AUTH_STATES.computeIfAbsent(testId, k -> new AuthState());
    }

    protected final Config config;
    protected final String baseUrl;
    protected final String testId;

    public Config getConfig() { return config; }
    protected final Map<String, String> defaultHeaders = new HashMap<>();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final RestAssuredConfig REST_CONFIG = RestAssuredConfig.config()
        .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
            (cls, charset) -> OBJECT_MAPPER
        ));

    public BaseApiClient(Config config)
    {
        this.config = config;
        this.baseUrl = config.getRunTimeProperty("apiBasePath", "");
        this.testId = config.testcaseName;

        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("Accept", "application/json");

        syncAuthFromSharedState();
    }

    public BaseApiClient(Config config, String customBaseUrl)
    {
        this.config = config;
        this.baseUrl = customBaseUrl;
        this.testId = config.testcaseName;

        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("Accept", "application/json");

        syncAuthFromSharedState();
    }

    // ========== AUTH STATE SHARING ==========

    public void setAuthToken(String token)
    {
        defaultHeaders.put("Authorization", "Bearer " + token);
        getOrCreateAuth(testId).token = token;
    }

    public void setBusinessUuid(String businessUuid)
    {
        defaultHeaders.put("x-business-uuid", businessUuid);
        getOrCreateAuth(testId).businessUuid = businessUuid;
    }

    public void setPersonUuid(String personUuid)
    {
        defaultHeaders.put("x-person-uuid", personUuid);
        getOrCreateAuth(testId).personUuid = personUuid;
    }

    public void setDebitAccountUuid(String uuid)
    {
        defaultHeaders.put("x-account-uuid", uuid);
        getOrCreateAuth(testId).debitAccountUuid = uuid;
    }

    protected void syncAuthFromSharedState()
    {
        AuthState state = AUTH_STATES.get(testId);
        if (state == null) return;

        if (state.token != null && !defaultHeaders.containsKey("Authorization"))
        {
            defaultHeaders.put("Authorization", "Bearer " + state.token);
        }
        if (state.businessUuid != null && !defaultHeaders.containsKey("x-business-uuid"))
        {
            defaultHeaders.put("x-business-uuid", state.businessUuid);
        }
        if (state.personUuid != null && !defaultHeaders.containsKey("x-person-uuid"))
        {
            defaultHeaders.put("x-person-uuid", state.personUuid);
        }
        if (state.debitAccountUuid != null && !defaultHeaders.containsKey("x-account-uuid"))
        {
            defaultHeaders.put("x-account-uuid", state.debitAccountUuid);
        }
        state.customHeaders.forEach((k, v) -> defaultHeaders.putIfAbsent(k, v));
    }

    // ========== REQUEST BUILDER ==========

    protected RequestSpecification buildRequest()
    {
        syncAuthFromSharedState();
        return RestAssured.given()
            .config(REST_CONFIG)
            .baseUri(baseUrl)
            .headers(defaultHeaders)
            .contentType(ContentType.JSON);
    }

    protected RequestSpecification buildRequest(Map<String, String> extraHeaders)
    {
        syncAuthFromSharedState();
        Map<String, String> merged = new HashMap<>(defaultHeaders);
        merged.putAll(extraHeaders);
        return RestAssured.given()
            .config(REST_CONFIG)
            .baseUri(baseUrl)
            .headers(merged)
            .contentType(ContentType.JSON);
    }

    // ========== HTTP METHODS ==========

    public Response get(String endpoint)
    {
        Response response = buildRequest().get(endpoint);
        logResponse("GET", endpoint, response);
        return response;
    }

    public Response get(String endpoint, Map<String, ?> queryParams)
    {
        Response response = buildRequest().queryParams(queryParams).get(endpoint);
        logResponse("GET", endpoint, response);
        return response;
    }

    public Response getWithHeaders(String endpoint, Map<String, String> extraHeaders)
    {
        Response response = buildRequest(extraHeaders).get(endpoint);
        logResponse("GET", endpoint, response);
        return response;
    }

    public Response post(String endpoint, Object body)
    {
        logRequestBody(body);
        Response response = body != null ? buildRequest().body(body).post(endpoint) : buildRequest().post(endpoint);
        logResponse("POST", endpoint, response);
        return response;
    }

    public Response post(String endpoint, Object body, Map<String, String> extraHeaders)
    {
        logRequestBody(body);
        Response response = body != null
            ? buildRequest(extraHeaders).body(body).post(endpoint)
            : buildRequest(extraHeaders).post(endpoint);
        logResponse("POST", endpoint, response);
        return response;
    }

    public Response put(String endpoint, Object body, Map<String, String> extraHeaders)
    {
        logRequestBody(body);
        Response response = buildRequest(extraHeaders).body(body).put(endpoint);
        logResponse("PUT", endpoint, response);
        return response;
    }

    public Response patch(String endpoint, Object body, Map<String, String> extraHeaders)
    {
        logRequestBody(body);
        Response response = buildRequest(extraHeaders).body(body).patch(endpoint);
        logResponse("PATCH", endpoint, response);
        return response;
    }

    public Response delete(String endpoint, Map<String, String> extraHeaders)
    {
        Response response = buildRequest(extraHeaders).delete(endpoint);
        logResponse("DELETE", endpoint, response);
        return response;
    }

    public Response put(String endpoint, Object body)
    {
        logRequestBody(body);
        Response response = buildRequest().body(body).put(endpoint);
        logResponse("PUT", endpoint, response);
        return response;
    }

    public Response patch(String endpoint, Object body)
    {
        logRequestBody(body);
        Response response = buildRequest().body(body).patch(endpoint);
        logResponse("PATCH", endpoint, response);
        return response;
    }

    public Response delete(String endpoint)
    {
        Response response = buildRequest().delete(endpoint);
        logResponse("DELETE", endpoint, response);
        return response;
    }

    // ========== RESPONSE HELPERS ==========

    public void assertSuccess(Response response)
    {
        int status = response.getStatusCode();
        AssertHelper.assertTrue(config, status >= 200 && status < 300,
            "API response should be 2xx, got " + status);
    }

    public void assertStatus(Response response, int expectedStatus)
    {
        AssertHelper.assertEquals(config, response.getStatusCode(), expectedStatus,
            "API status code verification");
    }

    // ========== LOGGING ==========

    private void logRequestBody(Object body)
    {
        if (body == null) return;
        try
        {
            String json = (body instanceof String) ? (String) body : OBJECT_MAPPER.writeValueAsString(body);
            Log.debug(config, "Request body: " + json);
        }
        catch (Exception ignored) {}
    }

    protected void logResponse(String method, String endpoint, Response response)
    {
        int status = response.getStatusCode();
        if (status >= 200 && status < 300)
        {
            Log.pass(config, "API " + method + " " + endpoint + " -> " + status);
        }
        else
        {
            Log.warning(config, "API " + method + " " + endpoint + " -> " + status);
        }
        String body = response.getBody().asString();
        Log.debug(config, "Response: " + body.substring(0, Math.min(500, body.length())));
    }

    public static ObjectMapper getObjectMapper()
    {
        return OBJECT_MAPPER;
    }
}
