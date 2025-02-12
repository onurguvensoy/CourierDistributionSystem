package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.example.courierdistributionsystem.util.PasswordEncoder;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.User.UserRole;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/settings/update")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> settings, HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Received settings update request for user: {}", username);
        
        if (username == null) {
            logger.warn("Attempt to update settings without authentication");
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "User not authenticated"
            ));
        }

        try {
            Map<String, Object> response = userService.updateUserSettings(username, settings);
            logger.info("Successfully updated settings for user: {}", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update settings for user: {}. Error: {}", username, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/settings/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData, HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Received password change request for user: {}", username);
        
        if (username == null) {
            logger.warn("Attempt to change password without authentication");
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "User not authenticated"
            ));
        }

        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            logger.warn("Invalid password change request for user: {} - missing password data", username);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Missing password data"
            ));
        }

        try {
            Map<String, Object> response = userService.changePassword(username, currentPassword, newPassword);
            logger.info("Successfully changed password for user: {}", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to change password for user: {}. Error: {}", username, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/api/user/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
   
            if (request.containsKey("email")) {
                user.setEmail(request.get("email"));
            }
            
 
            if (request.containsKey("phoneNumber") && user.getRole() != UserRole.ADMIN) {
                user.setPhoneNumber(request.get("phoneNumber"));
            }
            
   
            if (request.containsKey("vehicleType") && user.getRole() == UserRole.COURIER) {
                ((Courier) user).setVehicleType(request.get("vehicleType"));
            }
            
            userService.updateUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/api/user/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body("Both current and new passwords are required");
            }
            
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }
            

            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(user);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 