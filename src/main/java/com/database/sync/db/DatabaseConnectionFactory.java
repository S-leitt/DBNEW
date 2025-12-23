package com.database.sync.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DatabaseConnectionFactory {

    private static final Map<String, DataSource> dataSourceMap = new HashMap<>();

    static {
        initDataSource();
    }

    private static void initDataSource() {
        // MySQL
        try {
            HikariConfig mysqlConfig = new HikariConfig();
            mysqlConfig.setJdbcUrl("jdbc:mysql://localhost:3306/exam_paper_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai");
            mysqlConfig.setUsername("root");
            mysqlConfig.setPassword("Ltt@021366");
            mysqlConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            mysqlConfig.setPoolName("MySQL-Pool");
            mysqlConfig.setMaximumPoolSize(10);
            mysqlConfig.setMinimumIdle(2);
            mysqlConfig.setConnectionTimeout(30000);
            mysqlConfig.setIdleTimeout(600000);
            mysqlConfig.setMaxLifetime(1800000);
            HikariDataSource mysqlDs = new HikariDataSource(mysqlConfig);
            // Test connection
            mysqlDs.getConnection().close();
            dataSourceMap.put("mysql", mysqlDs);
            log.info("Successfully initialized MySQL data source");
        } catch (Exception e) {
            log.error("Failed to initialize MySQL data source: {}", e.getMessage(), e);
        }

        // Oracle
        try {
            HikariConfig oracleConfig = new HikariConfig();
            oracleConfig.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:ORCLPDB");
            oracleConfig.setUsername("exam_paper_db");
            oracleConfig.setPassword("exam_paper_db");
            oracleConfig.setDriverClassName("oracle.jdbc.OracleDriver");
            oracleConfig.setPoolName("Oracle-Pool");
            oracleConfig.setMaximumPoolSize(10);
            oracleConfig.setMinimumIdle(2);
            oracleConfig.setConnectionTimeout(30000);
            oracleConfig.setIdleTimeout(600000);
            oracleConfig.setMaxLifetime(1800000);
            HikariDataSource oracleDs = new HikariDataSource(oracleConfig);
            // Test connection
            oracleDs.getConnection().close();
            dataSourceMap.put("oracle", oracleDs);
            log.info("Successfully initialized Oracle data source");
        } catch (Exception e) {
            log.error("Failed to initialize Oracle data source: {}", e.getMessage(), e);
        }

        // SQL Server
        try {
            HikariConfig sqlServerConfig = new HikariConfig();
            sqlServerConfig.setJdbcUrl("jdbc:sqlserver://localhost:1434;databaseName=CAP_1324;trustServerCertificate=true");
            sqlServerConfig.setUsername("sa");
            sqlServerConfig.setPassword("021366");
            sqlServerConfig.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            sqlServerConfig.setPoolName("SQLServer-Pool");
            sqlServerConfig.setMaximumPoolSize(10);
            sqlServerConfig.setMinimumIdle(2);
            sqlServerConfig.setConnectionTimeout(30000);
            sqlServerConfig.setIdleTimeout(600000);
            sqlServerConfig.setMaxLifetime(1800000);
            HikariDataSource sqlServerDs = new HikariDataSource(sqlServerConfig);
            // Test connection
            sqlServerDs.getConnection().close();
            dataSourceMap.put("sqlserver", sqlServerDs);
            log.info("Successfully initialized SQL Server data source");
        } catch (Exception e) {
            log.error("Failed to initialize SQL Server data source: {}", e.getMessage(), e);
        }
    }

    public static Connection getConnection(String databaseId) throws SQLException {
        DataSource dataSource = getDataSource(databaseId);
        if (dataSource == null) {
            throw new SQLException("Invalid database ID: " + databaseId);
        }
        return dataSource.getConnection();
    }

    public static DataSource getDataSource(String databaseId) {
        return dataSourceMap.get(databaseId);
    }

    public static void closeDataSource(String databaseId) {
        DataSource dataSource = dataSourceMap.remove(databaseId);
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            log.info("Closed data source for database: {}", databaseId);
        }
    }

    public static void closeAllDataSources() {
        for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            DataSource dataSource = entry.getValue();
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
                log.info("Closed data source for database: {}", entry.getKey());
            }
        }
        dataSourceMap.clear();
    }
}