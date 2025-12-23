package com.example.dbnew.mapper;

import com.example.dbnew.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper {

    @Select("SELECT id, name, credit, teacher FROM course")
    List<Course> findAll();

    @Insert("INSERT INTO course(name, credit, teacher) VALUES(#{name}, #{credit}, #{teacher})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Course course);

    @Delete("DELETE FROM course")
    void deleteAll();
}
