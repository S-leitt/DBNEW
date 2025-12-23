package com.example.dbnew.mapper;

import com.example.dbnew.entity.Score;
import com.example.dbnew.entity.StudentScoreDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ScoreMapper {

    @Select("SELECT id, student_id, course_id, score, term FROM score")
    List<Score> findAll();

    @Insert("INSERT INTO score(student_id, course_id, score, term) VALUES(#{studentId}, #{courseId}, #{score}, #{term})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Score score);

    @Delete("DELETE FROM score")
    void deleteAll();

    @Select("""
            SELECT summary.student_id AS studentId,
                   s.name AS studentName,
                   c.name AS courseName,
                   summary.total_score AS totalScore,
                   summary.rank AS rank
            FROM (
                SELECT sc.student_id,
                       SUM(sc.score) AS total_score,
                       DENSE_RANK() OVER (ORDER BY SUM(sc.score) DESC) AS rank
                FROM score sc
                GROUP BY sc.student_id
            ) summary
            JOIN student s ON s.id = summary.student_id
            JOIN score sc2 ON sc2.student_id = summary.student_id
            JOIN course c ON c.id = sc2.course_id
            WHERE summary.rank <= 5
            ORDER BY summary.rank, summary.total_score DESC
            """)
    List<StudentScoreDetail> findTopFiveRankedStudents();
}
