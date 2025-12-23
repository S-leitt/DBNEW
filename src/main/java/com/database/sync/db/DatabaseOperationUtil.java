package com.database.sync.db;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DatabaseOperationUtil {

    private static final Set<String> missingDataSourceLogged = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static List<Map<String, Object>> executeQuery(String databaseId, String sql, Object... params) {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DatabaseConnectionFactory.getConnection(databaseId);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            log.error("Error executing query on database {}: {}", databaseId, e.getMessage(), e);
        }
        return result;
    }

    public static int executeUpdate(String databaseId, String sql, Object... params) {
        int rowsAffected = 0;
        try (Connection conn = DatabaseConnectionFactory.getConnection(databaseId);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, params);
            rowsAffected = ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error executing update on database {}: {}", databaseId, e.getMessage(), e);
        }
        return rowsAffected;
    }

    public static Map<String, Object> executeSingleRowQuery(String databaseId, String sql, Object... params) {
        List<Map<String, Object>> result = executeQuery(databaseId, sql, params);
        return result.isEmpty() ? null : result.get(0);
    }

    public static Object executeScalar(String databaseId, String sql, Object... params) {
        try (Connection conn = DatabaseConnectionFactory.getConnection(databaseId);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error executing scalar query on database {}: {}", databaseId, e.getMessage(), e);
        }
        return null;
    }

    public static boolean tableExists(String databaseId, String tableName) {
        String sql = "";
        DatabaseType dbType = DatabaseType.fromString(databaseId);

        switch (dbType) {
            case MYSQL -> {
                sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
            }
            case ORACLE -> {
                sql = "SELECT COUNT(*) FROM user_tables WHERE table_name = UPPER(?)";
            }
            case SQLSERVER -> {
                sql = "SELECT COUNT(*) FROM sys.tables WHERE name = ?";
            }
        }

        return ((Number) executeScalar(databaseId, sql, tableName)).intValue() > 0;
    }

    public static List<String> getTableNames(String databaseId) {
        List<String> tableNames = new ArrayList<>();
        String sql = "";
        DatabaseType dbType = DatabaseType.fromString(databaseId);

        switch (dbType) {
            case MYSQL -> {
                sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()";
            }
            case ORACLE -> {
                sql = "SELECT table_name FROM user_tables";
            }
            case SQLSERVER -> {
                sql = "SELECT name FROM sys.tables";
            }
        }

        List<Map<String, Object>> result = executeQuery(databaseId, sql);
        for (Map<String, Object> row : result) {
            tableNames.add(row.values().iterator().next().toString());
        }
        return tableNames;
    }

    private static void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }

    public static Connection getConnection(String databaseId) throws SQLException {
        return DatabaseConnectionFactory.getConnection(databaseId);
    }

    public static boolean testConnection(String databaseId) {
        try (Connection conn = DatabaseConnectionFactory.getDataSource(databaseId) != null
                ? DatabaseConnectionFactory.getConnection(databaseId)
                : null) {
            if (conn == null) {
                if (missingDataSourceLogged.add(databaseId)) {
                    String reason = DatabaseConnectionFactory.getInitializationError(databaseId);
                    if (reason != null) {
                        log.warn("Data source for database {} is unavailable: {}", databaseId, reason);
                    } else {
                        log.warn("No data source configured for database ID: {}", databaseId);
                    }
                }
                return false;
            }
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            log.error("Connection test failed for database {}: {}", databaseId, e.getMessage());
            return false;
        }
    }
}
