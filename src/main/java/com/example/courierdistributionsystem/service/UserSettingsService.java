package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.jpa.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.HashMap;

@Service
public class UserSettingsService {
    private static final Logger logger = LoggerFactory.getLogger(UserSettingsService.class);

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Map<String, Object> updateUserSettings(String username, Map<String, String> settings) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (settings.containsKey("email")) {
                user.setEmail(settings.get("email"));
            }
            
            if (settings.containsKey("phoneNumber")) {
                user.setPhoneNumber(settings.get("phoneNumber"));
            }

            userRepository.save(user);
            
            response.put("status", "success");
            response.put("message", "Settings updated successfully");
        } catch (Exception e) {
            logger.error("Error updating user settings: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "Failed to update settings: " + e.getMessage());
        }
        
        return response;
    }

    @Transactional
    public Map<String, Object> changePassword(String username, String currentPassword, String newPassword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
                response.put("status", "error");
                response.put("message", "Current password is incorrect");
                return response;
            }

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashedPassword);
            userRepository.save(user);
            
            response.put("status", "success");
            response.put("message", "Password changed successfully");
        } catch (Exception e) {
            logger.error("Error changing password: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "Failed to change password: " + e.getMessage());
        }
        
        return response;
    }
} 