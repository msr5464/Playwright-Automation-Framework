package automation.core;

import automation.core.Enums.*;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * User pool management with query builder DSL and database-driven user allocation.
 */
public class UserManagement
{

    private static String environment = null;
    private static Country defaultCountry = Country.SG;

    private static final int MAX_RETRIES = 5;
    private static final int RETRY_BASE_DELAY_MS = 10000;
    private static final int RETRY_RANDOM_MS = 2000;

    public static void initialize(String env, Country country)
    {
        environment = env;
        defaultCountry = country;
        Log.info("UserManagement initialized: env=" + env + ", country=" + country);
    }

    /**
     * Get a free user using the fluent query builder DSL.
     */
    public static User getFreeUser(Config config, String testcaseName, java.util.function.Function<UserQueryBuilder, UserQueryBuilder> queryBuilderFn)
    {
        UserQueryBuilder builder = new UserQueryBuilder(testcaseName);
        UserQueryBuilder finalBuilder = queryBuilderFn.apply(builder);
        return getUserWithRetry(config, finalBuilder);
    }

    /**
     * Release user back to pool.
     */
    public static void releaseUser(Config config, int userId)
    {
        if (environment == null) return;
        String query = "UPDATE `users_" + environment + "` SET usageStatus = 'FREE', testcaseName = NULL WHERE id = " + userId;
        try
        {
            DatabaseHelper.executeQuery(config, query, QueryType.update, DatabaseName.Thanos);
        }
        catch (Exception e)
        {
            Log.error("Failed to release user " + userId + ": " + e.getMessage());
        }
    }

    public static void releaseUsers(Config config, List<Integer> userIds)
    {
        for (int userId : userIds)
        {
            releaseUser(config, userId);
        }
    }

    private static User getUserWithRetry(Config config, UserQueryBuilder builder)
    {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++)
        {
            try
            {
                String selectQuery = builder.buildSelectQuery(environment);
                ResultSet rs = (ResultSet) DatabaseHelper.executeQuery(config, selectQuery, QueryType.select, DatabaseName.Thanos);
                if (rs != null && rs.next())
                {
                    int userId = rs.getInt("id");
                    String updateQuery = builder.buildUpdateQuery(environment, userId);
                    DatabaseHelper.executeQuery(config, updateQuery, QueryType.update, DatabaseName.Thanos);

                    return User.builder()
                        .id(userId)
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .businessName(rs.getString("businessName"))
                        .personReferenceCode(rs.getString("personReferenceCode"))
                        .businessReferenceCode(rs.getString("businessReferenceCode"))
                        .fullName(rs.getString("fullName"))
                        .userType(UserType.valueOf(rs.getString("userType")))
                        .feature(parseFeature(rs.getString("feature")))
                        .country(Country.valueOf(rs.getString("country").toUpperCase()))
                        .isPoolUser("YES".equals(rs.getString("poolUser")))
                        .build();
                }
            }
            catch (Exception e)
            {
                Log.error("User fetch attempt " + attempt + "/" + MAX_RETRIES + " failed: " + e.getMessage());
            }

            if (attempt < MAX_RETRIES)
            {
                try
                {
                    long delay = RETRY_BASE_DELAY_MS + (long)(Math.random() * RETRY_RANDOM_MS);
                    Thread.sleep(delay);
                }
                catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("Failed to get free user after " + MAX_RETRIES + " attempts. Builder: " + builder);
    }

    // ========== QUERY BUILDER ==========

    public static class UserQueryBuilder
    {
        private final String testcaseName;
        private UserType userType = UserType.Any;
        private Integer specificUserId = null;
        private final List<Feature> features = new ArrayList<>();
        private Country country = null;
        private final List<Country> excludedCountries = new ArrayList<>();
        private final List<Feature> excludedFeatures = new ArrayList<>();
        private final List<UserType> excludedUserTypes = new ArrayList<>();

        public UserQueryBuilder(String testcaseName)
        {
            this.testcaseName = testcaseName;
        }

        public UserQueryBuilder withUserType(UserType type)
        {
            this.userType = type;
            return this;
        }

        public UserQueryBuilder withFeature(Feature feature)
        {
            this.features.add(feature);
            return this;
        }

        public UserQueryBuilder withCountry(Country country)
        {
            this.country = country;
            return this;
        }

        public UserQueryBuilder withUserId(int userId)
        {
            this.specificUserId = userId;
            return this;
        }

        public UserQueryBuilder withoutCountry(Country country)
        {
            this.excludedCountries.add(country);
            return this;
        }

        public UserQueryBuilder withoutFeature(Feature feature)
        {
            this.excludedFeatures.add(feature);
            return this;
        }

        public UserQueryBuilder withoutUserType(UserType type)
        {
            this.excludedUserTypes.add(type);
            return this;
        }

        String buildSelectQuery(String env)
        {
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `users_").append(env).append("` WHERE usageStatus = 'FREE'");

            if (specificUserId != null)
            {
                query.append(" AND id = ").append(specificUserId);
            }
            if (userType != UserType.Any)
            {
                query.append(" AND userType = '").append(userType.name()).append("'");
            }
            for (Feature f : features)
            {
                query.append(" AND feature LIKE '%").append(f.name()).append("%'");
            }
            Country c = this.country != null ? this.country : defaultCountry;
            query.append(" AND country = '").append(c.name()).append("'");

            for (Country ec : excludedCountries)
            {
                query.append(" AND country != '").append(ec.name()).append("'");
            }
            for (Feature ef : excludedFeatures)
            {
                query.append(" AND feature NOT LIKE '%").append(ef.name()).append("%'");
            }
            for (UserType et : excludedUserTypes)
            {
                query.append(" AND userType != '").append(et.name()).append("'");
            }

            query.append(" LIMIT 1");
            return query.toString();
        }

        String buildUpdateQuery(String env, int userId)
        {
            return "UPDATE `users_" + env + "` SET usageStatus = 'BUSY', testcaseName = '" + testcaseName + "' WHERE id = " + userId;
        }

        @Override
        public String toString()
        {
            return "UserQuery[type=" + userType + ", features=" + features + ", country=" + country + "]";
        }
    }

    /**
     * Parse feature from database SET column. Takes the first valid feature from comma-separated values.
     */
    private static Feature parseFeature(String featureString)
    {
        if (featureString == null || featureString.trim().isEmpty())
        {
            return null;
        }
        
        // Split by comma and take the first valid feature
        String[] features = featureString.split(",");
        for (String f : features)
        {
            f = f.trim();
            try
            {
                return Feature.valueOf(f);
            }
            catch (IllegalArgumentException e)
            {
                // Feature not in enum, continue to next
                continue;
            }
        }
        return null; // No valid feature found
    }
}
