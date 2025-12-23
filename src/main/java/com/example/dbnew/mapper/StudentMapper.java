package com.example.dbnew.mapper;

import com.example.dbnew.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentMapper {

    @Select("SELECT id, name, age, email, enrolled_at FROM student")
    List<Student> findAll();

    @Insert("INSERT INTO student(name, age, email, enrolled_at) VALUES(#{name}, #{age}, #{email}, #{enrolledAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Student student);

    @Delete("DELETE FROM student")
    void deleteAll();
}
