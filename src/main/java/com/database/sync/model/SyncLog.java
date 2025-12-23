package com.database.sync.model;

import lombok.Data;

import java.util.Date;

@Data
public class SyncLog {
    private Long id;
    private String syncType;
    private String sourceDatabase;
    private String targetDatabase;
    private String tableName;
    private Integer totalRecords;
    private Integer successRecords;
    private Integer failedRecords;
    private String status;
    private Date startTime;
    private Date endTime;
    private String errorMessage;
    private String syncResult;
}