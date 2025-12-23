package com.database.sync.controller;

import com.database.sync.model.User;
import com.database.sync.service.UserPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permission")
@Slf4j
public class PermissionController {

    @Autowired
    private UserPermissionService userPermissionService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> loginInfo) {
        String username = (String) loginInfo.get("username");
        String password = (String) loginInfo.get("password");

        User user = userPermissionService.authenticate(username, password);
        if (user != null) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "登录成功",
                    "user", Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "realName", user.getRealName(),
                            "email", user.getEmail(),
                            "phone", user.getPhone()
                    )
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "用户名或密码错误"));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
        User user = userPermissionService.getUserById(userId);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "用户不存在"));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userPermissionService.getAllUsers();
        return ResponseEntity.ok(Map.of("success", true, "users", users));
    }

    @PostMapping("/user")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        User createdUser = userPermissionService.createUser(user);
        if (createdUser != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "用户创建成功", "user", createdUser));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "用户名已存在"));
        }
    }

    @PutMapping("/user")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        User updatedUser = userPermissionService.updateUser(user);
        if (updatedUser != null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "用户更新成功", "user", updatedUser));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "用户更新失败"));
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        boolean deleted = userPermissionService.deleteUser(userId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "用户删除成功"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "用户删除失败"));
        }
    }

    @PutMapping("/user/{userId}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long userId, @RequestBody Map<String, String> passwordInfo) {
        String oldPassword = passwordInfo.get("oldPassword");
        String newPassword = passwordInfo.get("newPassword");

        boolean updated = userPermissionService.updatePassword(userId, oldPassword, newPassword);
        if (updated) {
            return ResponseEntity.ok(Map.of("success", true, "message", "密码更新成功"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "密码更新失败，原密码错误"));
        }
    }
}
