package com.example.courierdistributionsystem.controller;
import org.springframework.data.domain.Pageable;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> users = userService.getAllUsers();
            response.put("status", "success");
            response.put("data", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch users: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> userRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!userRequest.containsKey("username") || !userRequest.containsKey("email") || 
                !userRequest.containsKey("password") || !userRequest.containsKey("role")) {
                response.put("status", "error");
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userService.createUser(userRequest);
            response.put("status", "success");
            response.put("data", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.deleteUser(id);
            response.put("status", "success");
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> userRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.updateUser(id, userRequest);
            response.put("status", "success");
            response.put("data", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/by-role/{userRole}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String userRole, Pageable pageable) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> users = userService.getUsersByRole(userRole, pageable);
            response.put("status", "success");
            response.put("data", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch users by role: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
