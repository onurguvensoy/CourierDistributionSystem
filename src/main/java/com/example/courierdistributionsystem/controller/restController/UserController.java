package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.service.IUserService;
import com.example.courierdistributionsystem.model.User;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Failed to get all users: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers() {
        try {
            return ResponseEntity.ok(userService.getAllCustomers());
        } catch (Exception e) {
            logger.error("Failed to get all customers: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/couriers")
    public ResponseEntity<?> getAllCouriers() {
        try {
            return ResponseEntity.ok(userService.getAllCouriers());
        } catch (Exception e) {
            logger.error("Failed to get all couriers: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins() {
        try {
            return ResponseEntity.ok(userService.getAllAdmins());
        } catch (Exception e) {
            logger.error("Failed to get all admins: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            return ResponseEntity.ok(userService.getUserStats());
        } catch (Exception e) {
            logger.error("Failed to get user stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/settings/update")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> settings, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            logger.warn("Unauthorized attempt to update settings");
            return ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "Unauthorized access"
            ));
        }

        try {
            Map<String, Object> response = userService.updateUserSettings(username, settings);
            logger.info("Successfully updated settings for user: {}", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update settings for user {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/settings/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            logger.warn("Unauthorized attempt to change password");
            return ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "Unauthorized access"
            ));
        }

        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Current password and new password are required"
            ));
        }

        try {
            Map<String, Object> response = userService.changePassword(username, currentPassword, newPassword);
            logger.info("Successfully changed password for user: {}", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to change password for user {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                return ResponseEntity.status(401).body("Unauthorized access");
            }

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (request.containsKey("email")) {
                String newEmail = request.get("email");
                if (userService.existsByEmail(newEmail) && !newEmail.equals(user.getEmail())) {
                    return ResponseEntity.badRequest().body("Email already exists");
                }
                user.setEmail(newEmail);
            }
            
            userService.updateUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            userService.deleteUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editUser(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        try {
            User updatedUser = userService.editUser(id, updates);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 