package com.example.courierdistributionsystem.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PasswordEncoderService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordEncoderService.class);
    private static final int WORKLOAD = 12; // Default workload factor for BCrypt
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 72; // BCrypt's maximum input length
    
    public String encode(String rawPassword) {
        validatePassword(rawPassword);
        
        try {
            // Generate a salt with fixed workload and hash the password
            String salt = BCrypt.gensalt(WORKLOAD);
            String hashedPassword = BCrypt.hashpw(rawPassword, salt);
            
            // Verify the hash was generated correctly
            if (!BCrypt.checkpw(rawPassword, hashedPassword)) {
                logger.error("Generated hash verification failed");
                throw new IllegalStateException("Failed to generate valid password hash");
            }
            
            return hashedPassword;
        } catch (IllegalArgumentException e) {
            logger.error("Failed to hash password: Invalid argument - {}", e.getMessage());
            throw new IllegalArgumentException("Invalid password format");
        } catch (Exception e) {
            logger.error("Failed to hash password: {}", e.getMessage());
            throw new IllegalStateException("Failed to process password");
        }
    }
    
    public boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            logger.error("Raw password or hashed password is null");
            return false;
        }
        
        if (hashedPassword.trim().isEmpty()) {
            logger.error("Hashed password is empty");
            return false;
        }
        
        try {
            // Validate the hash format before checking
            if (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2b$") && !hashedPassword.startsWith("$2y$")) {
                logger.error("Invalid hash format: Does not start with BCrypt identifier");
                return false;
            }
            
            // BCrypt's checkpw method handles salt extraction and verification
            return BCrypt.checkpw(rawPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid hash format: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error verifying password: {}", e.getMessage());
            return false;
        }
    }

    private void validatePassword(String password) {
        if (password == null) {
            logger.error("Password is null");
            throw new IllegalArgumentException("Password cannot be null");
        }

        if (password.trim().isEmpty()) {
            logger.error("Password is empty");
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            logger.error("Password is too short");
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            logger.error("Password exceeds maximum length");
            throw new IllegalArgumentException("Password cannot be longer than " + MAX_PASSWORD_LENGTH + " characters");
        }
    }
} 