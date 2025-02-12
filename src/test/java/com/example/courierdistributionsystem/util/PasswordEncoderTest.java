package com.example.courierdistributionsystem.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new PasswordEncoder();

    @Test
    void testPasswordEncoding() {
        String rawPassword = "testPassword123";
        
        // Test encoding
        String encodedPassword = passwordEncoder.encode(rawPassword);
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        
        // Test matching
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword));
    }
} 