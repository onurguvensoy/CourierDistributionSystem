package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class for managing user-related operations.
 */
@Service
@Validated
public class UserService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserManagementService userManagementService;
    private final PasswordEncoderService passwordEncoder;
    private final Counter userSignupCounter;
    private final Counter userSignupFailureCounter;
    private final Timer userLookupTimer;

    public UserService(UserManagementService userManagementService, 
                      PasswordEncoderService passwordEncoder,
                      MeterRegistry meterRegistry) {
        this.userManagementService = userManagementService;
        this.passwordEncoder = passwordEncoder;
        
        // Initialize metrics
        this.userSignupCounter = Counter.builder("user.signup.total")
                .description("Total number of user signups")
                .register(meterRegistry);
        
        this.userSignupFailureCounter = Counter.builder("user.signup.failures")
                .description("Number of failed user signups")
                .register(meterRegistry);
        
        this.userLookupTimer = Timer.builder("user.lookup.time")
                .description("Time taken to look up users")
                .register(meterRegistry);
    }
    
    /**
     * Retrieves a user by their username.
     *
     * @param username the username to search for
     * @return the found user
     * @throws UserNotFoundException if no user is found with the given username
     */
    @Cacheable(value = "users", key = "#username")
    public User getUserByUsername(@NotBlank String username) {
        return userLookupTimer.record(() -> 
            userManagementService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username))
        );
    }

    /**
     * Registers a new user in the system.
     *
     * @param signupRequest the signup request containing user details
     * @return a map containing the result of the signup operation
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public Map<String, String> signup(Map<String, String> signupRequest) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String username = signupRequest.get("username");
            String email = signupRequest.get("email");
            String password = signupRequest.get("password");
            String role = signupRequest.get("role");

            // Validate required fields
            if (username == null || username.trim().isEmpty()) {
                userSignupFailureCounter.increment();
                response.put("error", "Username is required");
                return response;
            }
            if (email == null || email.trim().isEmpty()) {
                userSignupFailureCounter.increment();
                response.put("error", "Email is required");
                return response;
            }
            if (password == null || password.trim().isEmpty()) {
                userSignupFailureCounter.increment();
                response.put("error", "Password is required");
                return response;
            }
            if (role == null || role.trim().isEmpty()) {
                userSignupFailureCounter.increment();
                response.put("error", "Role is required");
                return response;
            }

            // Check if username already exists
            if (userManagementService.existsByUsername(username)) {
                userSignupFailureCounter.increment();
                response.put("error", "Username already exists");
                return response;
            }

            // Check if email already exists
            if (userManagementService.existsByEmail(email)) {
                userSignupFailureCounter.increment();
                response.put("error", "Email already exists");
                return response;
            }

            String encodedPassword = passwordEncoder.encode(password);
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            
            switch (userRole) {
                case CUSTOMER -> {
                    Customer customer = Customer.builder()
                            .username(username)
                            .email(email)
                            .password(encodedPassword)
                            .role(userRole)
                            .phoneNumber(signupRequest.get("phoneNumber"))
                            .deliveryAddress(signupRequest.get("deliveryAddress"))
                            .build();
                    userManagementService.saveCustomer(customer);
                }
                case COURIER -> {
                    Courier courier = Courier.builder()
                            .username(username)
                            .email(email)
                            .password(encodedPassword)
                            .role(userRole)
                            .phoneNumber(signupRequest.get("phoneNumber"))
                            .vehicleType(signupRequest.get("vehicleType"))
                            .available(true)
                            .build();
                    userManagementService.saveCourier(courier);
                }
                case ADMIN -> {
                    Admin admin = Admin.builder()
                            .username(username)
                            .email(email)
                            .password(encodedPassword)
                            .role(userRole)
                            .build();
                    userManagementService.saveAdmin(admin);
                }
                default -> {
                    userSignupFailureCounter.increment();
                    throw new IllegalArgumentException("Invalid role: " + role);
                }
            }

            userSignupCounter.increment();
            response.put("message", "User registered successfully");
            return response;
            
        } catch (IllegalArgumentException e) {
            userSignupFailureCounter.increment();
            LOGGER.error("Invalid role specified: {}", e.getMessage());
            response.put("error", "Invalid role specified");
            return response;
        } catch (Exception e) {
            userSignupFailureCounter.increment();
            LOGGER.error("Error during user registration: {}", e.getMessage());
            response.put("error", "An unexpected error occurred");
            return response;
        }
    }
    
    /**
     * Custom exception for user not found scenarios.
     */
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
} 