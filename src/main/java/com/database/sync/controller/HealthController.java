package com.database.sync.controller;

import com.database.sync.db.DatabaseOperationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping({"", "/", "/check"})
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Boolean> databaseStatus = new HashMap<>();

        // Test database connections
        databaseStatus.put("mysql", DatabaseOperationUtil.testConnection("mysql"));
        databaseStatus.put("oracle", DatabaseOperationUtil.testConnection("oracle"));
        databaseStatus.put("sqlserver", DatabaseOperationUtil.testConnection("sqlserver"));

        result.put("status", "UP");
        result.put("databaseStatus", databaseStatus);
        result.put("message", "Database Sync System is running");

        return ResponseEntity.ok(result);
    }
}
