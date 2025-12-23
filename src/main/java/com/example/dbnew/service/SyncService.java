package com.example.dbnew.service;

import com.example.dbnew.context.DataSourceKey;
import com.example.dbnew.context.DynamicDataSourceContextHolder;
import com.example.dbnew.entity.*;
import com.example.dbnew.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final ScoreMapper scoreMapper;
    private final UserMapper userMapper;
    private final SyncLogMapper syncLogMapper;

    public void syncAllAsync(DataSourceKey source, DataSourceKey target) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> syncAll(source, target));
        }
    }

    @Transactional
    public void syncAll(DataSourceKey source, DataSourceKey target) {
        SyncLog log = SyncLog.builder()
                .source(source.name())
                .target(target.name())
                .status("STARTED")
                .message("Synchronization triggered manually")
                .syncedAt(LocalDateTime.now())
                .build();
        syncLogMapper.insert(log);

        try {
            List<Student> students = readFromSource(source, () -> studentMapper.findAll());
            List<Course> courses = readFromSource(source, () -> courseMapper.findAll());
            List<Score> scores = readFromSource(source, () -> scoreMapper.findAll());
            List<User> users = readFromSource(source, () -> userMapper.findAll());

            writeToTarget(target, () -> {
                studentMapper.deleteAll();
                courseMapper.deleteAll();
                scoreMapper.deleteAll();
                userMapper.deleteAll();

                students.forEach(studentMapper::insert);
                courses.forEach(courseMapper::insert);
                scores.forEach(scoreMapper::insert);
                users.forEach(userMapper::insert);
            });

            log.setStatus("SUCCESS");
            log.setMessage("Synchronization completed");
        } catch (Exception ex) {
            log.setStatus("FAILED");
            log.setMessage(ex.getMessage());
            throw ex;
        } finally {
            log.setSyncedAt(LocalDateTime.now());
            syncLogMapper.insert(log);
            DynamicDataSourceContextHolder.clear();
        }
    }

    private <T> T readFromSource(DataSourceKey source, SupplierWithException<T> supplier) {
        DynamicDataSourceContextHolder.setDataSource(source);
        try {
            return supplier.get();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read from " + source, ex);
        }
    }

    private void writeToTarget(DataSourceKey target, RunnableWithException action) {
        DynamicDataSourceContextHolder.setDataSource(target);
        try {
            action.run();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to write to " + target, ex);
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    private interface RunnableWithException {
        void run() throws Exception;
    }
}
