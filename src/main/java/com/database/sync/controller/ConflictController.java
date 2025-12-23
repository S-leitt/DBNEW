package com.database.sync.controller;

import com.database.sync.model.ConflictRecord;
import com.database.sync.service.ConflictResolutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conflict")
@Slf4j
public class ConflictController {

    @Autowired
    private ConflictResolutionService conflictResolutionService;

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingConflicts() {
        try {
            List<ConflictRecord> conflicts = conflictResolutionService.getPendingConflicts();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "conflicts", conflicts
            ));
        } catch (Exception e) {
            log.error("Error getting pending conflicts: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "获取待处理冲突失败: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllConflicts() {
        try {
            List<ConflictRecord> conflicts = conflictResolutionService.getAllConflicts();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "conflicts", conflicts
            ));
        } catch (Exception e) {
            log.error("Error getting all conflicts: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "获取所有冲突失败: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{conflictId}")
    public ResponseEntity<?> getConflictById(@PathVariable Long conflictId) {
        try {
            ConflictRecord conflict = conflictResolutionService.getConflictById(conflictId);
            if (conflict != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "conflict", conflict
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "冲突记录不存在"
                ));
            }
        } catch (Exception e) {
            log.error("Error getting conflict by id {}: {}", conflictId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "获取冲突详情失败: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/resolve/{conflictId}")
    public ResponseEntity<?> resolveConflict(
            @PathVariable Long conflictId,
            @RequestBody Map<String, String> resolutionInfo) {
        String resolutionStrategy = resolutionInfo.get("resolutionStrategy");
        String resolvedBy = resolutionInfo.get("resolvedBy");

        try {
            boolean resolved = conflictResolutionService.resolveConflict(conflictId, resolutionStrategy, resolvedBy);
            if (resolved) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "冲突解决成功"
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "success", false,
                        "message", "冲突解决失败"
                ));
            }
        } catch (Exception e) {
            log.error("Error resolving conflict {}: {}", conflictId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "冲突解决失败: " + e.getMessage()
            ));
        }
    }
}
