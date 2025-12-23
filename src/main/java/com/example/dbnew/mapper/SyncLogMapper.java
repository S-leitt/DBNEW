package com.example.dbnew.mapper;

import com.example.dbnew.entity.SyncLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface SyncLogMapper {

    @Insert("INSERT INTO sync_log(source, target, status, message, synced_at) VALUES(#{source}, #{target}, #{status}, #{message}, #{syncedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SyncLog log);
}
