package com.database.sync.controller;

import com.database.sync.sync.SyncManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sync")
@Slf4j
public class SyncController {

    @Autowired
    private SyncManager syncManager;

    @PostMapping("/all")
    public ResponseEntity<?> syncAllDatabases() {
        try {
            syncManager.syncAllDatabases();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "已启动全量数据库同步任务"
            ));
        } catch (Exception e) {
            log.error("Error starting sync all databases: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "启动同步任务失败: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/database")
    public ResponseEntity<?> syncDatabase(@RequestBody Map<String, String> syncInfo) {
        String sourceDatabase = syncInfo.get("sourceDatabase");
        String targetDatabase = syncInfo.get("targetDatabase");

        try {
            syncManager.syncDatabase(sourceDatabase, targetDatabase);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", String.format("已启动从 %s 到 %s 的数据库同步任务", sourceDatabase, targetDatabase)
            ));
        } catch (Exception e) {
            log.error("Error starting sync database: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "启动同步任务失败: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/table")
    public ResponseEntity<?> syncTable(@RequestBody Map<String, String> syncInfo) {
        String sourceDatabase = syncInfo.get("sourceDatabase");
        String targetDatabase = syncInfo.get("targetDatabase");
        String tableName = syncInfo.get("tableName");

        try {
            syncManager.syncSpecificTable(sourceDatabase, targetDatabase, tableName);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", String.format("已启动从 %s 到 %s 的表 %s 同步任务", sourceDatabase, targetDatabase, tableName)
            ));
        } catch (Exception e) {
            log.error("Error starting sync table: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "启动同步任务失败: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getSyncLogs() {
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "logs", syncManager.getSyncLogs()
            ));
        } catch (Exception e) {
            log.error("Error getting sync logs: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "获取同步日志失败: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getSyncStatus() {
        try {
            // Implement sync status checking
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "status", "running",
                    "message", "同步服务运行正常"
            ));
        } catch (Exception e) {
            log.error("Error getting sync status: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "获取同步状态失败: " + e.getMessage()
            ));
        }
    }
}
