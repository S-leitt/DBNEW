package com.database.sync.sync;

import com.database.sync.db.DatabaseOperationUtil;
import com.database.sync.model.SyncLog;
import com.database.sync.service.ConflictResolutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class SyncManager {

    @Autowired
    private ConflictResolutionService conflictResolutionService;

    private final ExecutorService syncExecutor = Executors.newFixedThreadPool(3);

    @Scheduled(cron = "0 0 1 * * ?") // Daily 1 AM sync
    public void scheduledSyncAllDatabases() {
        log.info("Starting scheduled full database sync");
        syncAllDatabases();
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void checkSyncStatus() {
        log.info("Checking sync status");
        // Implement sync status checking logic
    }

    public void syncAllDatabases() {
        List<String> databases = List.of("mysql", "oracle", "sqlserver");
        
        for (String sourceDb : databases) {
            for (String targetDb : databases) {
                if (!sourceDb.equals(targetDb)) {
                    syncExecutor.submit(() -> {
                        syncDatabase(sourceDb, targetDb);
                    });
                }
            }
        }
    }

    public void syncDatabase(String sourceDatabase, String targetDatabase) {
        log.info("Starting sync from {} to {}", sourceDatabase, targetDatabase);
        
        SyncLog syncLog = new SyncLog();
        syncLog.setSyncType("full");
        syncLog.setSourceDatabase(sourceDatabase);
        syncLog.setTargetDatabase(targetDatabase);
        syncLog.setStatus("running");
        syncLog.setStartTime(new Date());
        
        try {
            List<String> sourceTables = DatabaseOperationUtil.getTableNames(sourceDatabase);
            
            for (String tableName : sourceTables) {
                SyncTask syncTask = new SyncTask(sourceDatabase, targetDatabase, tableName, conflictResolutionService);
                syncTask.execute();
            }
            
            syncLog.setStatus("success");
            syncLog.setEndTime(new Date());
        } catch (Exception e) {
            log.error("Error syncing from {} to {}: {}", sourceDatabase, targetDatabase, e.getMessage(), e);
            syncLog.setStatus("failed");
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setEndTime(new Date());
        } finally {
            saveSyncLog(syncLog);
        }
    }

    public void syncSpecificTable(String sourceDatabase, String targetDatabase, String tableName) {
        log.info("Starting sync for table {} from {} to {}", tableName, sourceDatabase, targetDatabase);
        
        SyncLog syncLog = new SyncLog();
        syncLog.setSyncType("table");
        syncLog.setSourceDatabase(sourceDatabase);
        syncLog.setTargetDatabase(targetDatabase);
        syncLog.setTableName(tableName);
        syncLog.setStatus("running");
        syncLog.setStartTime(new Date());
        
        try {
            SyncTask syncTask = new SyncTask(sourceDatabase, targetDatabase, tableName, conflictResolutionService);
            syncTask.execute();
            
            syncLog.setStatus("success");
            syncLog.setEndTime(new Date());
        } catch (Exception e) {
            log.error("Error syncing table {} from {} to {}: {}", tableName, sourceDatabase, targetDatabase, e.getMessage(), e);
            syncLog.setStatus("failed");
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setEndTime(new Date());
        } finally {
            saveSyncLog(syncLog);
        }
    }

    private void saveSyncLog(SyncLog syncLog) {
        String sql = "INSERT INTO sync_logs (sync_type, source_database, target_database, table_name, status, start_time, end_time, error_message) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        DatabaseOperationUtil.executeUpdate("mysql", sql, 
                syncLog.getSyncType(), syncLog.getSourceDatabase(), syncLog.getTargetDatabase(), 
                syncLog.getTableName(), syncLog.getStatus(), syncLog.getStartTime(), 
                syncLog.getEndTime(), syncLog.getErrorMessage());
    }

    public List<SyncLog> getSyncLogs() {
        String sql = "SELECT * FROM sync_logs ORDER BY start_time DESC LIMIT 100";
        List<java.util.Map<String, Object>> logMaps = DatabaseOperationUtil.executeQuery("mysql", sql);
        return logMaps.stream().map(this::mapToSyncLog).toList();
    }

    private SyncLog mapToSyncLog(java.util.Map<String, Object> logMap) {
        SyncLog syncLog = new SyncLog();
        syncLog.setId((Long) logMap.get("id"));
        syncLog.setSyncType((String) logMap.get("sync_type"));
        syncLog.setSourceDatabase((String) logMap.get("source_database"));
        syncLog.setTargetDatabase((String) logMap.get("target_database"));
        syncLog.setTableName((String) logMap.get("table_name"));
        syncLog.setStatus((String) logMap.get("status"));
        syncLog.setStartTime((Date) logMap.get("start_time"));
        syncLog.setEndTime((Date) logMap.get("end_time"));
        syncLog.setErrorMessage((String) logMap.get("error_message"));
        return syncLog;
    }
}
