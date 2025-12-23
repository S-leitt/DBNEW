package com.database.sync.db;

public enum DatabaseType {
    MYSQL("mysql"),
    ORACLE("oracle"),
    SQLSERVER("sqlserver");

    private final String type;

    DatabaseType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static DatabaseType fromString(String type) {
        for (DatabaseType dbType : DatabaseType.values()) {
            if (dbType.type.equalsIgnoreCase(type)) {
                return dbType;
            }
        }
        throw new IllegalArgumentException("Invalid database type: " + type);
    }
}