package com.example.courierdistributionsystem.service;
import com.example.courierdistributionsystem.dto.SignupRequest;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import com.example.courierdistributionsystem.model.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Service
@Validated
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final PasswordEncoderService passwordEncoder;

    @Autowired
    public AuthService(UserService userService, PasswordEncoderService passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> login(String username, String password) {
        logger.debug("Processing login request for user: {}", username);
        
        try {
            User user = userService.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new AuthenticationException.InvalidCredentialsException();
                });

            if (password == null || password.trim().isEmpty()) {
                logger.warn("Empty password provided for user: {}", username);
                throw new AuthenticationException.InvalidCredentialsException();
            }

            if (!passwordEncoder.matches(password, passwordEncoder.encode(password))) {
                logger.warn("Invalid password for user: {}", username);
                throw new AuthenticationException.InvalidCredentialsException();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().name());
            response.put("userId", user.getId());

            switch (user.getRole()) {
                case CUSTOMER -> {
                    Customer customer = (Customer) user;
                    response.put("phoneNumber", customer.getPhoneNumber());
                }
                case COURIER -> {
                    Courier courier = (Courier) user;
                    response.put("phoneNumber", courier.getPhoneNumber());
                    response.put("isAvailable", courier.isAvailable());
                }
                case ADMIN -> {
                    // Add any admin-specific attributes if needed
                }
            }

            logger.info("Login successful for user: {}", username);
            return response;

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for user: {} - {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user: {} - {}", username, e.getMessage(), e);
            throw new AuthenticationException("An unexpected error occurred during login", "LOGIN_ERROR");
        }
    }

    @Transactional
    public Map<String, String> signup(@Valid SignupRequest request) {
        logger.debug("Processing signup request for user: {}", request.getUsername());
        
        try {
            // Check if username or email already exists
            if (userService.existsByUsername(request.getUsername())) {
                logger.warn("Username already exists: {}", request.getUsername());
                throw new AuthenticationException.UserAlreadyExistsException("Username already exists");
            }

            if (userService.existsByEmail(request.getEmail())) {
                logger.warn("Email already exists: {}", request.getEmail());
                throw new AuthenticationException.UserAlreadyExistsException("Email already exists");
            }

            // Convert request to appropriate user type and save
            Map<String, String> signupData = new HashMap<>();
            signupData.put("username", request.getUsername());
            signupData.put("email", request.getEmail());
            signupData.put("password", request.getPassword());
            signupData.put("role", request.getRole());
            signupData.put("phoneNumber", request.getPhoneNumber());
            signupData.put("vehicleType", request.getVehicleType());

            userService.signup(signupData);

            logger.info("Signup successful for user: {}", request.getUsername());
            return Map.of("message", "User registered successfully");

        } catch (AuthenticationException e) {
            logger.warn("Signup failed for user: {} - {}", request.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during signup for user: {} - {}", request.getUsername(), e.getMessage(), e);
            throw new AuthenticationException("An unexpected error occurred during signup", "SIGNUP_ERROR");
        }
    }
}
