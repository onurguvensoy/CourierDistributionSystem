package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.SignupRequest;
import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import jakarta.servlet.http.HttpSession;
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

    @Autowired
    private HttpSession httpSession;

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

            logger.debug("Attempting to find user: {}", username);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                logger.warn("Login failed: User not found for username: {}", username);
                throw new AuthenticationException("Invalid username or password");
            }

            logger.debug("User found, checking password match");
            if (!passwordEncoder.matches(password, user.getPassword())) {
                logger.warn("Login failed: Invalid password for username: {}", username);
                throw new AuthenticationException("Invalid username or password");
            }

            logger.info("Login successful for user: {}", username);
            
            // Only include necessary fields in response
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("userId", user.getId());
            response.put("email", user.getEmail());

            if (user instanceof Courier) {
                Courier courier = (Courier) user;
                response.put("phoneNumber", courier.getPhoneNumber());
                response.put("isAvailable", courier.isAvailable());
                response.put("vehicleType", courier.getVehicleType());
            } else if (user instanceof Customer) {
                Customer customer = (Customer) user;
                response.put("phoneNumber", customer.getPhoneNumber());
                response.put("deliveryAddress", customer.getDeliveryAddress());
            }

            // Store in session
            httpSession.setAttribute("username", username);
            httpSession.setAttribute("role", user.getRole());
            httpSession.setAttribute("userId", user.getId());
            httpSession.setAttribute("email", user.getEmail());

            return response;

        } catch (AuthenticationException e) {
            logger.error("Authentication error for user {}: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user {}: {}", username, e.getMessage(), e);
            throw new AuthenticationException("An error occurred during login: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, String> signup(SignupRequest request) {
        Map<String, String> response = new HashMap<>();

        try {
            logger.info("Processing signup request for username: {}", request.getUsername());

            // Check for existing username and email
            if (userService.existsByUsername(request.getUsername())) {
                logger.warn("Signup failed: Username already exists: {}", request.getUsername());
                throw new AuthenticationException("Username already exists");
            }

            if (userService.existsByEmail(request.getEmail())) {
                logger.warn("Signup failed: Email already exists: {}", request.getEmail());
                throw new AuthenticationException("Email already exists");
            }

            String hashedPassword = passwordEncoder.encode(request.getPassword());
            logger.debug("Password hashed successfully");

            switch (request.getRoleType()) {
                case CUSTOMER -> {
                    if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
                        throw new AuthenticationException("Phone number is required for customers");
                    }
                    if (request.getDeliveryAddress() == null || request.getDeliveryAddress().trim().isEmpty()) {
                        throw new AuthenticationException("Delivery address is required for customers");
                    }
                    Customer customer = Customer.builder()
                            .username(request.getUsername())
                            .email(request.getEmail())
                            .password(hashedPassword)
                            .role(User.UserRole.CUSTOMER)
                            .phoneNumber(request.getPhoneNumber())
                            .deliveryAddress(request.getDeliveryAddress())
                            .build();
                    userService.saveCustomer(customer);
                    logger.info("Customer registered successfully: {}", request.getUsername());
                }
                case COURIER -> {
                    if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
                        throw new AuthenticationException("Phone number is required for couriers");
                    }
                    if (request.getVehicleType() == null || request.getVehicleType().trim().isEmpty()) {
                        throw new AuthenticationException("Vehicle type is required for couriers");
                    }
                    Courier courier = Courier.builder()
                            .username(request.getUsername())
                            .email(request.getEmail())
                            .password(hashedPassword)
                            .role(User.UserRole.COURIER)
                            .phoneNumber(request.getPhoneNumber())
                            .vehicleType(request.getVehicleType())
                            .available(true)
                            .build();
                    userService.saveCourier(courier);
                    logger.info("Courier registered successfully: {}", request.getUsername());
                }
                case ADMIN -> {
                    Admin admin = Admin.builder()
                            .username(request.getUsername())
                            .email(request.getEmail())
                            .password(hashedPassword)
                            .role(User.UserRole.ADMIN)
                            .build();
                    userService.saveAdmin(admin);
                    logger.info("Admin registered successfully: {}", request.getUsername());
                }
            }

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

    public void logoutUser() {
        try {
            if (httpSession != null) {
                String username = (String) httpSession.getAttribute("username");
                logger.info("Logging out user: {}", username);
                httpSession.invalidate();
                logger.info("Session invalidated successfully");
            } else {
                logger.warn("Logout attempted with no active session");
            }
        } catch (IllegalStateException e) {
            logger.warn("Session was already invalidated");
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage(), e);
            throw new AuthenticationException("An error occurred during logout");
        }
    }
}
