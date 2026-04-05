package automation.core;

import io.restassured.response.Response;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe token cache for reusing auth tokens across tests.
 * Avoids repeated login API calls by caching tokens per username until they expire.
 * Default expiry: 900 seconds (15 minutes), configurable via tokenExpiryTime property.
 */
public class TokenManagement
{
    private static final ConcurrentHashMap<String, String> userTokens = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Response> userResponses = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Date> tokenCreationTimes = new ConcurrentHashMap<>();
    private static final Object lock = new Object();

    public static void addToken(String username, String token)
    {
        synchronized (lock)
        {
            String oldToken = userTokens.get(username);

            // If token is unchanged and creation time already recorded, keep it
            if (oldToken != null && oldToken.equals(token))
            {
                if (tokenCreationTimes.containsKey(username))
                {
                    return;
                }
            }

            userTokens.put(username, token);
            tokenCreationTimes.put(username, new Date());
        }
    }

    public static void removeToken(String username)
    {
        synchronized (lock)
        {
            userTokens.remove(username);
            tokenCreationTimes.remove(username);
        }
    }

    public static boolean isTokenExpired(Config testConfig, String username)
    {
        Date tokenCreationTime = tokenCreationTimes.get(username);
        if (tokenCreationTime == null)
        {
            Log.comment(testConfig, "Token creation time not found for user: " + username + ", considering expired");
            return true;
        }

        try
        {
            String expiryTimeStr = testConfig.getRunTimeProperty("tokenExpiryTime");
            if (expiryTimeStr == null || expiryTimeStr.isEmpty())
            {
                expiryTimeStr = "900"; // Default: 15 minutes
            }

            long tokenExpiryTime = Long.parseLong(expiryTimeStr);
            long currentTime = new Date().getTime();
            long tokenAge = (currentTime - tokenCreationTime.getTime()) / 1000;

            boolean isExpired = tokenAge > tokenExpiryTime;
            Log.comment(testConfig, String.format("Token age: %d seconds, expiry: %d seconds, expired: %s",
                    tokenAge, tokenExpiryTime, isExpired));

            return isExpired;
        }
        catch (NumberFormatException e)
        {
            Log.comment(testConfig, "Error parsing tokenExpiryTime, treating token as expired: " + e.getMessage());
            return true;
        }
    }

    /**
     * Returns the cached token for the given username if it exists and has not expired.
     * Returns null if no token is found or the token has expired (and removes it from the cache).
     */
    public static String getCurrentToken(Config testConfig, String username)
    {
        synchronized (lock)
        {
            Log.comment(testConfig, "Checking cached token for user: " + username);

            if (!userTokens.containsKey(username))
            {
                Log.comment(testConfig, "No token found for user: " + username);
                return null;
            }

            String currentToken = userTokens.get(username);

            if (isTokenExpired(testConfig, username))
            {
                Log.comment(testConfig, "Token expired for user: " + username + ", evicting from cache");
                userTokens.remove(username);
                tokenCreationTimes.remove(username);
                return null;
            }

            Log.comment(testConfig, "Returning valid cached token for user: " + username);
            return currentToken;
        }
    }

    public static void addResponse(String username, Response response)
    {
        userResponses.put(username, response);
    }

    public static Response getResponse(String username)
    {
        return userResponses.get(username);
    }

    public static void clearAllTokens()
    {
        synchronized (lock)
        {
            userTokens.clear();
            tokenCreationTimes.clear();
            userResponses.clear();
        }
    }
}
