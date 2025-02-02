package com.example.courierdistributionsystem.controller;
import com.example.courierdistributionsystem.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("status", "success");
            response.put("data", userManagementService.getAllUsers());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("status", "success");
            response.put("data", userManagementService.getAllCustomers());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/couriers")
    public ResponseEntity<?> getAllCouriers() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("status", "success");
            response.put("data", userManagementService.getAllCouriers());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("status", "success");
            response.put("data", userManagementService.getAllAdmins());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("status", "success");
            response.put("data", userManagementService.getUserStats());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestParam String role) {
        Map<String, Object> response = new HashMap<>();
        try {
            userManagementService.deleteUser(id, role);
            response.put("status", "success");
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @RequestParam String role) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("status", "success");
            response.put("data", userManagementService.getUserById(id, role));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 