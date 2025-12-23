package com.database.sync.model;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class ConflictRecord {
    private Long id;
    private String sourceDatabase;
    private String targetDatabase;
    private String tableName;
    private String primaryKeyName;
    private String primaryKeyValue;
    private Map<String, Object> sourceData;
    private Map<String, Object> targetData;
    private String conflictType;
    private String resolutionStrategy;
    private String resolvedBy;
    private Date resolvedTime;
    private String status;
    private Date createTime;
    private String conflictDetail;
}