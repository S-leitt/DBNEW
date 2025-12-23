package com.database.sync.sync;

import com.database.sync.db.DatabaseOperationUtil;
import com.database.sync.model.ConflictRecord;
import com.database.sync.service.ConflictResolutionService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class SyncTask {

    private final String sourceDatabase;
    private final String targetDatabase;
    private final String tableName;
    private final ConflictResolutionService conflictResolutionService;

    public SyncTask(String sourceDatabase, String targetDatabase, String tableName, ConflictResolutionService conflictResolutionService) {
        this.sourceDatabase = sourceDatabase;
        this.targetDatabase = targetDatabase;
        this.tableName = tableName;
        this.conflictResolutionService = conflictResolutionService;
    }

    public void execute() {
        log.info("Executing sync task: {} -> {} for table {}", sourceDatabase, targetDatabase, tableName);
        
        try {
            // Get primary key column
            String primaryKey = getPrimaryKey(tableName);
            if (primaryKey == null) {
                log.warn("Could not determine primary key for table {}", tableName);
                return;
            }
            
            // Get all records from source database
            List<Map<String, Object>> sourceRecords = DatabaseOperationUtil.executeQuery(sourceDatabase, "SELECT * FROM " + tableName);
            
            for (Map<String, Object> sourceRecord : sourceRecords) {
                Object primaryKeyValue = sourceRecord.get(primaryKey);
                
                // Check if record exists in target database
                Map<String, Object> targetRecord = DatabaseOperationUtil.executeSingleRowQuery(targetDatabase, 
                        "SELECT * FROM " + tableName + " WHERE " + primaryKey + " = ?", primaryKeyValue);
                
                if (targetRecord == null) {
                    // Insert new record
                    insertRecord(sourceRecord);
                } else {
                    // Update existing record if needed
                    updateRecord(sourceRecord, targetRecord, primaryKey);
                }
            }
        } catch (Exception e) {
            log.error("Error executing sync task for table {}: {}", tableName, e.getMessage(), e);
        }
    }

    private String getPrimaryKey(String tableName) {
        // Simple primary key detection - assumes single column primary key
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = ? AND CONSTRAINT_NAME = 'PRIMARY'";
        Map<String, Object> primaryKeyMap = DatabaseOperationUtil.executeSingleRowQuery(sourceDatabase, sql, tableName);
        return primaryKeyMap != null ? (String) primaryKeyMap.get("COLUMN_NAME") : null;
    }

    private void insertRecord(Map<String, Object> record) {
        // Build insert SQL
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        Object[] params = new Object[record.size()];
        int index = 0;
        
        for (Map.Entry<String, Object> entry : record.entrySet()) {
            if (index > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(entry.getKey());
            values.append("?");
            params[index++] = entry.getValue();
        }
        
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        DatabaseOperationUtil.executeUpdate(targetDatabase, sql, params);
    }

    private void updateRecord(Map<String, Object> sourceRecord, Map<String, Object> targetRecord, String primaryKey) {
        // Check if records are different
        boolean recordsDiffer = false;
        for (Map.Entry<String, Object> entry : sourceRecord.entrySet()) {
            String columnName = entry.getKey();
            Object sourceValue = entry.getValue();
            Object targetValue = targetRecord.get(columnName);
            
            if ((sourceValue == null && targetValue != null) || (sourceValue != null && !sourceValue.equals(targetValue))) {
                recordsDiffer = true;
                break;
            }
        }
        
        if (recordsDiffer) {
            // Build update SQL
            StringBuilder setClause = new StringBuilder();
            Object[] params = new Object[sourceRecord.size()];
            int index = 0;
            
            for (Map.Entry<String, Object> entry : sourceRecord.entrySet()) {
                String columnName = entry.getKey();
                if (!columnName.equals(primaryKey)) {
                    if (index > 0) {
                        setClause.append(", ");
                    }
                    setClause.append(columnName).append(" = ?");
                    params[index++] = entry.getValue();
                }
            }
            
            // Add primary key to params
            params[index] = sourceRecord.get(primaryKey);
            
            String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + primaryKey + " = ?";
            DatabaseOperationUtil.executeUpdate(targetDatabase, sql, params);
        }
    }

    private void recordConflict(Map<String, Object> sourceRecord, Map<String, Object> targetRecord, String primaryKey, String conflictType) {
        ConflictRecord conflict = new ConflictRecord();
        conflict.setSourceDatabase(sourceDatabase);
        conflict.setTargetDatabase(targetDatabase);
        conflict.setTableName(tableName);
        conflict.setPrimaryKeyName(primaryKey);
        conflict.setPrimaryKeyValue(sourceRecord.get(primaryKey).toString());
        conflict.setConflictType(conflictType);
        conflict.setConflictDetail("Conflict detected between source and target records");
        
        conflictResolutionService.recordConflict(conflict);
    }
}
