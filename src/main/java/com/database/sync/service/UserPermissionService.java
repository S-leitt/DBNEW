package com.database.sync.service;

import com.database.sync.db.DatabaseOperationUtil;
import com.database.sync.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserPermissionService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND status = 1";
        Map<String, Object> userMap = DatabaseOperationUtil.executeSingleRowQuery("mysql", sql, username);
        
        if (userMap == null) {
            return null;
        }
        
        User user = mapToUser(userMap);
        
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        
        return null;
    }

    public User getUserById(Long userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        Map<String, Object> userMap = DatabaseOperationUtil.executeSingleRowQuery("mysql", sql, userId);
        return userMap != null ? mapToUser(userMap) : null;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Map<String, Object> userMap = DatabaseOperationUtil.executeSingleRowQuery("mysql", sql, username);
        return userMap != null ? mapToUser(userMap) : null;
    }

    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY create_time DESC";
        List<Map<String, Object>> userMaps = DatabaseOperationUtil.executeQuery("mysql", sql);
        return userMaps.stream().map(this::mapToUser).toList();
    }

    public User createUser(User user) {
        if (getUserByUsername(user.getUsername()) != null) {
            return null;
        }
        
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        String sql = "INSERT INTO users (username, password, real_name, email, phone, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        int rowsAffected = DatabaseOperationUtil.executeUpdate("mysql", sql, 
                user.getUsername(), encryptedPassword, user.getRealName(), 
                user.getEmail(), user.getPhone(), 1, new Date(), new Date());
        
        if (rowsAffected > 0) {
            return getUserByUsername(user.getUsername());
        }
        
        return null;
    }

    public User updateUser(User user) {
        String sql = "UPDATE users SET real_name = ?, email = ?, phone = ?, status = ?, update_time = ? WHERE id = ?";
        
        int rowsAffected = DatabaseOperationUtil.executeUpdate("mysql", sql, 
                user.getRealName(), user.getEmail(), user.getPhone(), 
                user.getStatus(), new Date(), user.getId());
        
        if (rowsAffected > 0) {
            return getUserById(user.getId());
        }
        
        return null;
    }

    public boolean deleteUser(Long userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = DatabaseOperationUtil.executeUpdate("mysql", sql, userId);
        return rowsAffected > 0;
    }

    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        String encryptedPassword = passwordEncoder.encode(newPassword);
        String sql = "UPDATE users SET password = ?, update_time = ? WHERE id = ?";
        int rowsAffected = DatabaseOperationUtil.executeUpdate("mysql", sql, encryptedPassword, new Date(), userId);
        return rowsAffected > 0;
    }

    private User mapToUser(Map<String, Object> userMap) {
        User user = new User();
        user.setId((Long) userMap.get("id"));
        user.setUsername((String) userMap.get("username"));
        user.setPassword((String) userMap.get("password"));
        user.setRealName((String) userMap.get("real_name"));
        user.setEmail((String) userMap.get("email"));
        user.setPhone((String) userMap.get("phone"));
        user.setStatus((Integer) userMap.get("status"));
        user.setCreateTime((Date) userMap.get("create_time"));
        user.setUpdateTime((Date) userMap.get("update_time"));
        return user;
    }
}
