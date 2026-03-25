package automation.core.api;

/**
 * Defines an API endpoint with its HTTP method and path.
 * Each module creates an enum implementing this interface.
 *
 * Example:
 *   public enum CardApiDetails implements ApiDetails {
 *       CreateCard(Method.POST, "/v1/cards", 201),
 *       GetCard(Method.GET, "/v1/cards/{id}", 200),
 *       DeleteCard(Method.DELETE, "/v1/cards/{id}", 200);
 *   }
 */
public interface ApiDetails
{

    enum Method { GET, POST, PUT, PATCH, DELETE }

    Method getMethod();
    String getEndpoint();
    int getExpectedStatus();
}
