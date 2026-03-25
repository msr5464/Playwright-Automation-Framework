package automation.modules.cards.api;

import automation.core.api.ApiDetails;

/**
 * Card module API endpoints.
 *
 * Usage:
 *   api.execute(CardApi.CreateCard, cardData, CardData.class);
 *   api.execute(CardApi.GetCard.withPath("id", "card-123"), null, CardData.class);
 *   api.execute(CardApi.DeleteCard.withPath("id", "card-123"));
 */
public enum CardApi implements ApiDetails
{

    CreateCard(Method.POST, "/v1/cards", 201),
    GetCard(Method.GET, "/v1/cards/{id}", 200),
    ListCards(Method.GET, "/v1/cards", 200),
    UpdateCard(Method.PATCH, "/v1/cards/{id}", 200),
    DeleteCard(Method.DELETE, "/v1/cards/{id}", 200);

    private final Method method;
    private final String endpoint;
    private final int expectedStatus;

    CardApi(Method method, String endpoint, int expectedStatus)
    {
        this.method = method;
        this.endpoint = endpoint;
        this.expectedStatus = expectedStatus;
    }

    @Override public Method getMethod() { return method; }
    @Override public String getEndpoint() { return endpoint; }
    @Override public int getExpectedStatus() { return expectedStatus; }

    /**
     * Replace path parameters: CardApi.GetCard.withPath("id", "card-123")
     * Returns a new ApiDetails with the substituted endpoint.
     */
    public ApiDetails withPath(String param, String value)
    {
        String resolved = this.endpoint.replace("{" + param + "}", value);
        final Method m = this.method;
        final int s = this.expectedStatus;
        return new ApiDetails()
        {
            @Override public Method getMethod() { return m; }
            @Override public String getEndpoint() { return resolved; }
            @Override public int getExpectedStatus() { return s; }
        };
    }
}
