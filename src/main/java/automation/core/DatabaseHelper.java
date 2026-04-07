package automation.core;

import automation.core.Enums.DatabaseName;
import automation.core.Enums.QueryType;

import java.sql.*;
import java.util.*;

/**
 * MySQL database helper with named connections, ThreadLocal timing, retry support, and parameterized queries.
 */
public class DatabaseHelper
{

    private static final int SLOW_QUERY_WARNING_THRESHOLD = 5000;
    private static final int VERY_SLOW_QUERY_THRESHOLD = 10000;

    private static final ThreadLocal<Long> queryStartTime = new ThreadLocal<>();

    // Named static connections per database
    private static Connection automationConnection = null;
    private static Connection stagingConnection = null;
	


    public static synchronized Connection getConnection(Config config, DatabaseName databaseName)
    {
    	Connection connection = null;
    	synchronized (DatabaseHelper.class)
        {
            connection = getExistingConnection(databaseName);
            try
            {
                if (connection == null || connection.isClosed())
                {
                    connection = createConnection(config, databaseName);
                    setConnection(databaseName, connection);
                }
            }
            catch (SQLException e)
            {
                config.logFail("Failed to get connection for " + databaseName + ": " + e.getMessage());
            }
        }
        return connection;
    }

    private static Connection getExistingConnection(DatabaseName databaseName)
    {
        return switch (databaseName)
        {
            case Automation -> automationConnection;
            case Staging -> stagingConnection;
        };
    }

    private static void setConnection(DatabaseName databaseName, Connection connection)
    {
        switch (databaseName)
        {
            case Automation -> automationConnection = connection;
            case Staging -> stagingConnection = connection;
        }
    }

    private static Connection createConnection(Config config, DatabaseName databaseName)
    {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url      = config.getRunTimeProperty("db." + databaseName.toString().toLowerCase() + ".url");
            String user     = config.getRunTimeProperty("db." + databaseName.toString().toLowerCase() + ".username");
            String password = config.getRunTimeProperty("db." + databaseName.toString().toLowerCase() + ".password");
            config.logComment("Connecting to database: " + databaseName + " (" + url + ")");
            return DriverManager.getConnection(url, user, password);
        }
        catch (ClassNotFoundException e)
        {
            config.logFail("MySQL driver not found: " + e.getMessage());
        }
        catch (SQLException e)
        {
            config.logFail("Unable to connect to " + databaseName + " DB: " + e.getMessage());
        }
        return null;
    }


    public static Object executeQuery(Config config, String sqlQuery, QueryType queryType, DatabaseName databaseName)
    {
        config.logComment("Executing query - '" + sqlQuery + "'");

        // Store start time in ThreadLocal for thread safety during parallel execution
        queryStartTime.set(System.currentTimeMillis());
        Connection connection = (Connection) getConnection(config, databaseName);
        Object returnValue = null;
        try
        {
            switch (queryType)
            {
                case select:
                    ResultSet resultSet = connection.createStatement().executeQuery(sqlQuery);
                    if (null == resultSet)
                    {
                        config.logWarning("No data was returned for above query");
                    }
                    returnValue = resultSet;
                    break;
                case update:
                    int recordsModified = connection.createStatement().executeUpdate(sqlQuery);
                    if (recordsModified == 0)
                    {
                        config.logWarning("No record updated for this query");
                    } else
                    {
                        config.logCommentForDebugging("Total record updated - " + recordsModified);
                    }
                    returnValue = recordsModified;
                    break;
                case delete:
                    returnValue = connection.createStatement().executeUpdate(sqlQuery);
                    config.logCommentForDebugging("Total records deleted - " + returnValue);
                    break;
                case create:
                    returnValue = connection.createStatement().executeUpdate(sqlQuery);
                    config.logCommentForDebugging("Table created successfully - " + returnValue);
                    break;
                case set:
                    returnValue = connection.createStatement().executeUpdate(sqlQuery);
                    config.logCommentForDebugging("Total records set - " + returnValue);
                    break;
            }
        }
        catch (SQLSyntaxErrorException e)
        {
            config.logFail("SQL syntax error: " + e.getMessage());
        }
        catch (DataTruncation e)
        {
            config.logWarning("Data truncation: " + e.getMessage());
        }
        catch (SQLException e)
        {
            config.logFail("Query failed: " + e.getMessage());
        }
        finally
        {
            // ThreadLocal cleanup prevents memory leaks during long-running test execution
            Long startTime = queryStartTime.get();
            if (startTime != null) {
                long executionTime = System.currentTimeMillis() - startTime;

                if (executionTime >= VERY_SLOW_QUERY_THRESHOLD) {
                    config.logWarning(String.format("VERY SLOW QUERY: Executed in %d ms (>%d ms threshold). Consider optimization!",
                        executionTime, VERY_SLOW_QUERY_THRESHOLD));
                } else if (executionTime >= SLOW_QUERY_WARNING_THRESHOLD) {
                    config.logWarning(String.format("SLOW QUERY: Executed in %d ms (>%d ms threshold). Monitor performance.",
                        executionTime, SLOW_QUERY_WARNING_THRESHOLD));
                } else {
                    config.logCommentForDebugging(String.format("Query executed in %d ms", executionTime));
                }

                queryStartTime.remove();
            }
        }
        return returnValue;
    }


    public static Map<String, String> executeSelectQuery(Config config, String query, DatabaseName databaseName)
    {
        ResultSet rs = (ResultSet) executeQuery(config, query, QueryType.select, databaseName);
        return createHashMapFromResultSet(config, rs);
    }


    public static Map<String, String> executeSelectQueryWithRetry(Config config, String query, DatabaseName databaseName)
    {
        int maxRetries = 5;
        int retryWaitMs = 3000;
        for (int attempt = 1; attempt <= maxRetries; attempt++)
        {
            Map<String, String> result = executeSelectQuery(config, query, databaseName);
            if (result != null && !result.isEmpty())
            {
                return result;
            }
            config.logComment("No result on attempt " + attempt + "/" + maxRetries + " for query: " + query);
            if (attempt < maxRetries)
            {
                try { Thread.sleep(retryWaitMs); } catch (InterruptedException ignored) {}
            }
        }
        config.logWarning("No result after " + maxRetries + " retries: " + query);
        return new LinkedHashMap<>();
    }


    public static Map<String, String> executeSelectQueryWithParams(Config config, String query, Map<String, Object> params, DatabaseName databaseName)
    {
        try
        {
            Connection connection = getConnection(config, databaseName);
            if (connection == null)
            {
                config.logFail("No connection available for " + databaseName);
                return new LinkedHashMap<>();
            }
            PreparedStatement ps = connection.prepareStatement(query);
            int index = 1;
            for (Object value : params.values())
            {
                ps.setObject(index++, value);
            }
            queryStartTime.set(System.currentTimeMillis());
            ResultSet rs = ps.executeQuery();
            long duration = System.currentTimeMillis() - queryStartTime.get();
            queryStartTime.remove();
            if (duration > SLOW_QUERY_WARNING_THRESHOLD)
            {
                config.logWarning("Slow parameterized query (" + duration + "ms) on " + databaseName + ": " + query);
            }
            return createHashMapFromResultSet(config, rs);
        }
        catch (SQLException e)
        {
            config.logFail("Parameterized query failed on " + databaseName + ": " + e.getMessage());
        }
        return new LinkedHashMap<>();
    }


    public static Map<String, String> createHashMapFromResultSet(Config config, ResultSet rs)
    {
        Map<String, String> result = new LinkedHashMap<>();
        try
        {
            if (rs != null && rs.next())
            {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++)
                {
                    result.put(metaData.getColumnName(i), rs.getString(i));
                }
            }
        }
        catch (SQLException e)
        {
            config.logFail("Failed to read ResultSet: " + e.getMessage());
        }
        return result;
    }


    public static List<Map<String, String>> executeSelectQueryAndReturnAllRows(Config config, String query, DatabaseName databaseName)
    {
        List<Map<String, String>> results = new ArrayList<>();
        try
        {
            ResultSet rs = (ResultSet) executeQuery(config, query, QueryType.select, databaseName);
            if (rs == null) return results;

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next())
            {
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++)
                {
                    row.put(metaData.getColumnName(i), rs.getString(i));
                }
                results.add(row);
            }
        }
        catch (SQLException e)
        {
            config.logFail("Query-to-list failed on " + databaseName + ": " + e.getMessage());
        }
        return results;
    }


    public static void closeAllConnections(Config config)
    {
        for (DatabaseName dbName : DatabaseName.values())
        {
            Connection conn = getExistingConnection(dbName);
            try
            {
                if (conn != null && !conn.isClosed())
                {
                    conn.close();
                    setConnection(dbName, null);
                    config.logComment("Closed DB connection: " + dbName);
                }
            }
            catch (SQLException e)
            {
                config.logFail("Error closing connection for " + dbName + ": " + e.getMessage());
            }
        }
    }
}
