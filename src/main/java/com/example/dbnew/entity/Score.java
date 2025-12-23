package com.example.dbnew.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Score {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Double score;
    private String term;
}
