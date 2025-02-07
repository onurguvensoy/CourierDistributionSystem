package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.SignupRequest;
import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoderService passwordEncoder;

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Login attempt for username: {}", username);
            
            if (username == null || username.trim().isEmpty()) {
                logger.warn("Login failed: Username is empty");
                throw new AuthenticationException("Username is required");
            }

            if (password == null || password.trim().isEmpty()) {
                logger.warn("Login failed: Password is empty");
                throw new AuthenticationException("Password is required");
            }

            User user = userService.findByUsername(username.trim());
            
            if (user == null) {
                logger.warn("Login failed: User not found for username: {}", username);
                throw new AuthenticationException("User not found for username: " + username);
            }

            String storedPassword = passwordEncoder.encode(password);
            if (storedPassword == null || storedPassword.trim().isEmpty()) {
                logger.error("Login failed: Stored password is null or empty for user: {}. This might indicate a database corruption or incomplete registration.", username);
                throw new AuthenticationException("Stored password is null or empty for user" + username);
            }

            logger.debug("Attempting to verify password for user: {}", username);
            boolean passwordMatches = passwordEncoder.matches(password, storedPassword);
            
            if (!passwordMatches) {
                logger.warn("Login failed: Invalid password for username: {}", username);
                throw new AuthenticationException("Invalid username or password");
            }

            response.put("success", true);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());

            switch (user.getRole()) {
                case COURIER -> {
                    Courier courier = (Courier) user;
                    response.put("phoneNumber", courier.getPhoneNumber());
                    response.put("isAvailable", courier.isAvailable());
                    response.put("vehicleType", courier.getVehicleType());
                }
                case CUSTOMER -> {
                    Customer customer = (Customer) user;
                    response.put("phoneNumber", customer.getPhoneNumber());
                    response.put("deliveryAddress", customer.getDeliveryAddress());
                }
                case ADMIN -> {
                    response.put("isAdmin", true);
                }
            }

            logger.info("Login successful for user: {}", username);
            return response;

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user {}: {}", username, e.getMessage(), e);
            throw new AuthenticationException("An unexpected error occurred during login");
        }
    }

    @Transactional
    public Map<String, String> signup(SignupRequest request) {
        Map<String, String> response = new HashMap<>();

        try {
            logger.info("Processing signup request for username: {}", request.getUsername());

            // Validate request
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                throw new AuthenticationException("Username is required");
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new AuthenticationException("Email is required");
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                throw new AuthenticationException("Password is required");
            }

            // Check for existing username and email
            if (userService.existsByUsername(request.getUsername())) {
                logger.warn("Signup failed: Username already exists: {}", request.getUsername());
                throw new AuthenticationException("Username already exists");
            }

            if (userService.existsByEmail(request.getEmail())) {
                logger.warn("Signup failed: Email already exists: {}", request.getEmail());
                throw new AuthenticationException("Email already exists");
            }

            // Validate password
            if (request.getPassword().length() < 6) {
                logger.warn("Signup failed: Password too short");
                throw new AuthenticationException("Password must be at least 6 characters long");
            }

            // Hash password
            String hashedPassword;
            try {
                hashedPassword = passwordEncoder.encode(request.getPassword());
                if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
                    logger.error("Password hashing failed: Result is null or empty");
                    throw new AuthenticationException("Error processing password");
                }
                logger.debug("Password hashed successfully");
            } catch (Exception e) {
                logger.error("Failed to hash password: {}", e.getMessage());
                throw new AuthenticationException("Error processing password");
            }

            // Create and save user based on role
            User savedUser = switch (request.getRoleType()) {
                case CUSTOMER -> {
                    validateCustomerFields(request);
                    Customer customer = Customer.builder()
                            .username(request.getUsername())
                            .email(request.getEmail())
                            .password(hashedPassword)
                            .role(User.UserRole.CUSTOMER)
                            .phoneNumber(request.getPhoneNumber())
                            .deliveryAddress(request.getDeliveryAddress())
                            .build();
                    yield userService.saveCustomer(customer);
                }
                case COURIER -> {
                    validateCourierFields(request);
                    Courier courier = Courier.builder()
                            .username(request.getUsername())
                            .email(request.getEmail())
                            .password(hashedPassword)
                            .role(User.UserRole.COURIER)
                            .phoneNumber(request.getPhoneNumber())
                            .vehicleType(request.getVehicleType())
                            .available(true)
                            .build();
                    yield userService.saveCourier(courier);
                }
                case ADMIN -> {
                    Admin admin = Admin.builder()
                            .username(request.getUsername())
                            .email(request.getEmail())
                            .password(hashedPassword)
                            .role(User.UserRole.ADMIN)
                            .build();
                    yield userService.saveAdmin(admin);
                }
            };

            if (savedUser == null || savedUser.getPassword() == null || savedUser.getPassword().trim().isEmpty()) {
                logger.error("User saved but password is null or empty for username: {}", request.getUsername());
                throw new AuthenticationException("Error during user registration");
            }

            logger.info("User registered successfully: {} with role: {}", request.getUsername(), request.getRoleType());
            response.put("message", "Registration successful");
            return response;

        } catch (AuthenticationException e) {
            logger.error("Authentication error during signup: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during signup: {}", e.getMessage(), e);
            throw new AuthenticationException("An error occurred during registration: " + e.getMessage());
        }
    }

    private void validateCustomerFields(SignupRequest request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new AuthenticationException("Phone number is required for customers");
        }
        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().trim().isEmpty()) {
            throw new AuthenticationException("Delivery address is required for customers");
        }
    }

    private void validateCourierFields(SignupRequest request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new AuthenticationException("Phone number is required for couriers");
        }
        if (request.getVehicleType() == null || request.getVehicleType().trim().isEmpty()) {
            throw new AuthenticationException("Vehicle type is required for couriers");
        }
    }
}
