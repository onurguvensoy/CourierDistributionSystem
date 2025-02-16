package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.SignupRequest;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.repository.jpa.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Service
@Validated
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> login(String username, String password) {
        logger.debug("Processing login request for user: {}", username);
        
        try {
            validateLoginCredentials(username, password);
            User user = getUserAndValidatePassword(username, password);
            return createLoginResponse(user);
        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for user: {} - {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user: {} - {}", username, e.getMessage(), e);
            throw new AuthenticationException("An unexpected error occurred during login", "LOGIN_ERROR");
        }
    }

    private void validateLoginCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            logger.warn("Empty credentials provided");
            throw new AuthenticationException.InvalidCredentialsException();
        }
    }

    private User getUserAndValidatePassword(String username, String password) {
        User user = userService.findByUsername(username)
            .orElseThrow(() -> {
                logger.warn("User not found with username: {}", username);
                return new AuthenticationException.InvalidCredentialsException();
            });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Invalid password for user: {}", username);
            throw new AuthenticationException.InvalidCredentialsException();
        }

        return user;
    }

    private Map<String, Object> createLoginResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());
        response.put("userId", user.getId());

        switch (user.getRole()) {
            case CUSTOMER -> addCustomerDetails((Customer) user, response);
            case COURIER -> addCourierDetails((Courier) user, response);
            case ADMIN -> validateAndAddAdminDetails(user.getUsername(), response);
        }

        logger.info("Login successful for user: {}", user.getUsername());
        return response;
    }

    private void addCustomerDetails(Customer customer, Map<String, Object> response) {
        response.put("phoneNumber", customer.getPhoneNumber());
    }

    private void addCourierDetails(Courier courier, Map<String, Object> response) {
        response.put("phoneNumber", courier.getPhoneNumber());
        response.put("isAvailable", courier.isAvailable());
    }

    private void validateAndAddAdminDetails(String username, Map<String, Object> response) {
        adminRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("Admin user found in users table but not in admin table: {}", username);
                return new AuthenticationException("Invalid admin account", "INVALID_ADMIN");
            });
        response.put("role", "ADMIN");
    }

    @Transactional
    public Map<String, String> signup(@Valid SignupRequest request) {
        logger.debug("Processing signup request for user: {}", request.getUsername());
        
        try {
            validateSignupRequest(request);
            return processSignup(request);
        } catch (AuthenticationException e) {
            logger.warn("Signup failed for user: {} - {}", request.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during signup for user: {} - {}", request.getUsername(), e.getMessage(), e);
            throw new AuthenticationException("An unexpected error occurred during signup", "SIGNUP_ERROR");
        }
    }

    private void validateSignupRequest(SignupRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            logger.warn("Username already exists: {}", request.getUsername());
            throw new AuthenticationException.UserAlreadyExistsException("Username already exists");
        }

        if (userService.existsByEmail(request.getEmail())) {
            logger.warn("Email already exists: {}", request.getEmail());
            throw new AuthenticationException.UserAlreadyExistsException("Email already exists");
        }
    }

    private Map<String, String> processSignup(SignupRequest request) {
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
    }
}
