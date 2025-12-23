package com.database.sync.controller;

import com.database.sync.service.DataManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@Slf4j
public class DataController {

    @Autowired
    private DataManagementService dataManagementService;

    @GetMapping("/tables/{databaseId}")
    public ResponseEntity<?> getTables(@PathVariable String databaseId) {
        try {
            List<String> tables = com.database.sync.db.DatabaseOperationUtil.getTableNames(databaseId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "tables", tables
            ));
        } catch (Exception e) {
            log.error("Error getting tables for database {}: {}", databaseId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "获取表列表失败: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/table/{databaseId}/{tableName}")
    public ResponseEntity<?> getTableData(
            @PathVariable String databaseId,
            @PathVariable String tableName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            List<Map<String, Object>> data = dataManagementService.getTableData(databaseId, tableName, page, pageSize);
            int total = dataManagementService.getTableDataCount(databaseId, tableName);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", data,
                    "total", total,
                    "page", page,
                    "pageSize", pageSize
            ));
        } catch (Exception e) {
            log.error("Error getting data for table {} in database {}: {}", tableName, databaseId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "获取表数据失败: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/table/{databaseId}/{tableName}/columns")
    public ResponseEntity<?> getTableColumns(@PathVariable String databaseId, @PathVariable String tableName) {
        try {
            List<String> columns = dataManagementService.getTableColumns(databaseId, tableName);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "columns", columns
            ));
        } catch (Exception e) {
            log.error("Error getting columns for table {} in database {}: {}", tableName, databaseId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "获取表列信息失败: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/table/{databaseId}/{tableName}")
    public ResponseEntity<?> insertData(@PathVariable String databaseId, @PathVariable String tableName, @RequestBody Map<String, Object> data) {
        try {
            boolean success = dataManagementService.insertData(databaseId, tableName, data);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "数据插入成功"
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "success", false,
                        "message", "数据插入失败"
                ));
            }
        } catch (Exception e) {
            log.error("Error inserting data into table {} in database {}: {}", tableName, databaseId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "数据插入失败: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/table/{databaseId}/{tableName}")
    public ResponseEntity<?> updateData(
            @PathVariable String databaseId,
            @PathVariable String tableName,
            @RequestBody Map<String, Object> data,
            @RequestParam String primaryKeyName,
            @RequestParam String primaryKeyValue) {
        try {
            boolean success = dataManagementService.updateData(databaseId, tableName, data, primaryKeyName, primaryKeyValue);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "数据更新成功"
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "success", false,
                        "message", "数据更新失败"
                ));
            }
        } catch (Exception e) {
            log.error("Error updating data in table {} in database {}: {}", tableName, databaseId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "数据更新失败: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/table/{databaseId}/{tableName}")
    public ResponseEntity<?> deleteData(
            @PathVariable String databaseId,
            @PathVariable String tableName,
            @RequestParam String primaryKeyName,
            @RequestParam String primaryKeyValue) {
        try {
            boolean success = dataManagementService.deleteData(databaseId, tableName, primaryKeyName, primaryKeyValue);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "数据删除成功"
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "success", false,
                        "message", "数据删除失败"
                ));
            }
        } catch (Exception e) {
            log.error("Error deleting data from table {} in database {}: {}", tableName, databaseId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "数据删除失败: " + e.getMessage()
            ));
        }
    }
}
