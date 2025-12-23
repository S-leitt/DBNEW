package com.database.sync.service;

import com.database.sync.db.DatabaseOperationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DataManagementService {

    public List<Map<String, Object>> getTableData(String databaseId, String tableName, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        String sql = String.format("SELECT * FROM %s LIMIT ? OFFSET ?", tableName);
        return DatabaseOperationUtil.executeQuery(databaseId, sql, pageSize, offset);
    }

    public int getTableDataCount(String databaseId, String tableName) {
        String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
        Object result = DatabaseOperationUtil.executeScalar(databaseId, sql);
        return result != null ? ((Number) result).intValue() : 0;
    }

    public boolean insertData(String databaseId, String tableName, Map<String, Object> data) {
        if (data.isEmpty()) {
            return false;
        }

        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        Object[] values = new Object[data.size()];
        int index = 0;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (index > 0) {
                columns.append(", ");
                placeholders.append(", ");
            }
            columns.append(entry.getKey());
            placeholders.append("?");
            values[index++] = entry.getValue();
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
        int rowsAffected = DatabaseOperationUtil.executeUpdate(databaseId, sql, values);
        return rowsAffected > 0;
    }

    public boolean updateData(String databaseId, String tableName, Map<String, Object> data, String primaryKeyName, Object primaryKeyValue) {
        if (data.isEmpty()) {
            return false;
        }

        StringBuilder setClause = new StringBuilder();
        Object[] values = new Object[data.size() + 1];
        int index = 0;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (index > 0) {
                setClause.append(", ");
            }
            setClause.append(entry.getKey()).append(" = ?");
            values[index++] = entry.getValue();
        }

        values[index] = primaryKeyValue;
        String sql = String.format("UPDATE %s SET %s WHERE %s = ?", tableName, setClause, primaryKeyName);
        int rowsAffected = DatabaseOperationUtil.executeUpdate(databaseId, sql, values);
        return rowsAffected > 0;
    }

    public boolean deleteData(String databaseId, String tableName, String primaryKeyName, Object primaryKeyValue) {
        String sql = String.format("DELETE FROM %s WHERE %s = ?", tableName, primaryKeyName);
        int rowsAffected = DatabaseOperationUtil.executeUpdate(databaseId, sql, primaryKeyValue);
        return rowsAffected > 0;
    }

    public Map<String, Object> getDataById(String databaseId, String tableName, String primaryKeyName, Object primaryKeyValue) {
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", tableName, primaryKeyName);
        return DatabaseOperationUtil.executeSingleRowQuery(databaseId, sql, primaryKeyValue);
    }

    public List<String> getTableColumns(String databaseId, String tableName) {
        String sql = String.format("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '%s' ORDER BY ORDINAL_POSITION", tableName);
        List<Map<String, Object>> columns = DatabaseOperationUtil.executeQuery(databaseId, sql);
        return columns.stream().map(col -> (String) col.get("COLUMN_NAME")).toList();
    }
}
