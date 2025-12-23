package com.example.dbnew.mapper;

import com.example.dbnew.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password, role FROM users")
    List<User> findAll();

    @Insert("INSERT INTO users(username, password, role) VALUES(#{username}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Delete("DELETE FROM users")
    void deleteAll();
}
