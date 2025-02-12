package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/settings/update")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> settings, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "User not authenticated"
            ));
        }

        Map<String, Object> response = userService.updateUserSettings(username, settings);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/settings/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "User not authenticated"
            ));
        }

        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Missing password data"
            ));
        }

        Map<String, Object> response = userService.changePassword(username, currentPassword, newPassword);
        return ResponseEntity.ok(response);
    }
} 