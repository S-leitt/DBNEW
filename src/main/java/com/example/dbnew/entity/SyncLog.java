package com.example.dbnew.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncLog {
    private Long id;
    private String source;
    private String target;
    private String status;
    private String message;
    private LocalDateTime syncedAt;
}
