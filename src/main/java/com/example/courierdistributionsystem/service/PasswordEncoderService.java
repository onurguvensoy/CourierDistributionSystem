package com.example.courierdistributionsystem.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncoderService {
    
    public String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
    
    public boolean matches(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (IllegalArgumentException e) {
            // This can happen if the stored hash is invalid
            return false;
        }
    }
} 