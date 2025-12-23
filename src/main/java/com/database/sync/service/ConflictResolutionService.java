package com.database.sync.service;

import com.database.sync.db.DatabaseOperationUtil;
import com.database.sync.model.ConflictRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ConflictResolutionService {

    public List<ConflictRecord> getPendingConflicts() {
        String sql = "SELECT * FROM conflict_records WHERE status = 'pending' ORDER BY create_time DESC";
        List<Map<String, Object>> conflictMaps = DatabaseOperationUtil.executeQuery("mysql", sql);
        return conflictMaps.stream().map(this::mapToConflictRecord).toList();
    }

    public ConflictRecord getConflictById(Long conflictId) {
        String sql = "SELECT * FROM conflict_records WHERE id = ?";
        Map<String, Object> conflictMap = DatabaseOperationUtil.executeSingleRowQuery("mysql", sql, conflictId);
        return conflictMap != null ? mapToConflictRecord(conflictMap) : null;
    }

    public boolean resolveConflict(Long conflictId, String resolutionStrategy, String resolvedBy) {
        String sql = "UPDATE conflict_records SET status = 'resolved', resolution_strategy = ?, resolved_by = ?, resolved_time = ? WHERE id = ?";
        int rowsAffected = DatabaseOperationUtil.executeUpdate("mysql", sql, resolutionStrategy, resolvedBy, new Date(), conflictId);
        return rowsAffected > 0;
    }

    public ConflictRecord recordConflict(ConflictRecord conflict) {
        String sql = "INSERT INTO conflict_records (source_database, target_database, table_name, primary_key_name, primary_key_value, conflict_type, conflict_detail, create_time, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        int rowsAffected = DatabaseOperationUtil.executeUpdate("mysql", sql, 
                conflict.getSourceDatabase(), conflict.getTargetDatabase(), 
                conflict.getTableName(), conflict.getPrimaryKeyName(), 
                conflict.getPrimaryKeyValue(), conflict.getConflictType(), 
                conflict.getConflictDetail(), new Date(), "pending");
        
        if (rowsAffected > 0) {
            return conflict;
        }
        
        return null;
    }

    public List<ConflictRecord> getResolvedConflicts() {
        String sql = "SELECT * FROM conflict_records WHERE status = 'resolved' ORDER BY resolved_time DESC";
        List<Map<String, Object>> conflictMaps = DatabaseOperationUtil.executeQuery("mysql", sql);
        return conflictMaps.stream().map(this::mapToConflictRecord).toList();
    }

    public List<ConflictRecord> getAllConflicts() {
        String sql = "SELECT * FROM conflict_records ORDER BY create_time DESC";
        List<Map<String, Object>> conflictMaps = DatabaseOperationUtil.executeQuery("mysql", sql);
        return conflictMaps.stream().map(this::mapToConflictRecord).toList();
    }

    private ConflictRecord mapToConflictRecord(Map<String, Object> conflictMap) {
        ConflictRecord conflict = new ConflictRecord();
        conflict.setId((Long) conflictMap.get("id"));
        conflict.setSourceDatabase((String) conflictMap.get("source_database"));
        conflict.setTargetDatabase((String) conflictMap.get("target_database"));
        conflict.setTableName((String) conflictMap.get("table_name"));
        conflict.setPrimaryKeyName((String) conflictMap.get("primary_key_name"));
        conflict.setPrimaryKeyValue((String) conflictMap.get("primary_key_value"));
        conflict.setConflictType((String) conflictMap.get("conflict_type"));
        conflict.setConflictDetail((String) conflictMap.get("conflict_detail"));
        conflict.setStatus((String) conflictMap.get("status"));
        conflict.setCreateTime((Date) conflictMap.get("create_time"));
        conflict.setResolvedTime((Date) conflictMap.get("resolved_time"));
        conflict.setResolvedBy((String) conflictMap.get("resolved_by"));
        conflict.setResolutionStrategy((String) conflictMap.get("resolution_strategy"));
        return conflict;
    }
}
