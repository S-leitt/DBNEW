package com.example.dbnew.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentScoreDetail {
    private Long studentId;
    private String studentName;
    private String courseName;
    private Double totalScore;
    private Integer rank;
}
