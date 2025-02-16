package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.dto.LoginRequest;
import com.example.courierdistributionsystem.dto.SignupRequest;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.service.AuthService;
import com.example.courierdistributionsystem.service.UserService;
import com.example.courierdistributionsystem.util.JwtUtils;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(AuthService authService, UserService userService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        logger.info("Login attempt for user: {}", username);
        
        try {
            // Validate request
            if (username == null || username.trim().isEmpty() || 
                loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                logger.warn("Invalid login request: Missing credentials");
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "INVALID_CREDENTIALS",
                    "message", "Username and password are required"
                ));
            }

            // Authenticate user
            Map<String, Object> loginResponse = authService.login(username, loginRequest.getPassword());
            
            if (loginResponse == null || !loginResponse.containsKey("role") || !loginResponse.containsKey("userId")) {
                throw new AuthenticationException("Invalid authentication response", "INVALID_AUTH_RESPONSE");
            }

            // Get role as string
            Object roleObj = loginResponse.get("role");
            String roleStr = (roleObj instanceof User.UserRole) ? ((User.UserRole) roleObj).name() : roleObj.toString();

            // Generate JWT token
            String token = jwtUtils.generateToken(
                username,
                roleStr,
                Long.valueOf(loginResponse.get("userId").toString())
            );
            
            // Create response with token and user info
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);
            response.put("role", roleStr);
            response.put("userId", loginResponse.get("userId"));
            
            logger.info("Login successful for user: {}", username);
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException.InvalidCredentialsException e) {
            logger.warn("Invalid credentials for user: {} - {}", username, e.getMessage());
            return ResponseEntity.status(401)
                .body(Map.of(
                    "error", "INVALID_CREDENTIALS",
                    "message", "Invalid username or password"
                ));
        } catch (AuthenticationException e) {
            logger.error("Authentication error for user: {} - {}", username, e.getMessage());
            return ResponseEntity.status(401)
                .body(Map.of(
                    "error", e.getErrorCode(),
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            logger.error("Unexpected error during login for user: {} - {}", username, e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred during login"
                ));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        String username = signupRequest.getUsername();
        logger.info("Processing signup request for username: {} with role: {}", username, signupRequest.getRole());
        
        try {
            logger.debug("Validating signup request data");
            validateSignupRequest(signupRequest);

            logger.debug("Calling auth service to process signup");
            Map<String, String> response = authService.signup(signupRequest);
            
            if (response.containsKey("error")) {
                logger.warn("Signup failed for user: {} - {}", username, response.get("error"));
                throw new AuthenticationException.InvalidUserDataException(response.get("error"));
            }
            
            logger.info("Signup successful for user: {}", username);
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException.UserAlreadyExistsException e) {
            logger.warn("User already exists: {} - {}", username, e.getMessage());
            return ResponseEntity.status(409).body(Map.of(
                "error", e.getErrorCode(),
                "message", e.getMessage()
            ));
        } catch (AuthenticationException.InvalidUserDataException e) {
            logger.warn("Invalid user data for: {} - {}", username, e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                "error", e.getErrorCode(),
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during signup for user: {} - {}", username, e.getMessage(), e);
            logger.error("Stack trace:", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "An unexpected error occurred during signup: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().body(Map.of(
            "status", "success",
            "message", "Logged out successfully"
        ));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        logger.info("Processing delete request for user: {}", username);
        
        try {
            userService.deleteByUsername(username);
            logger.info("User deleted successfully: {}", username);
            return ResponseEntity.ok().body(Map.of("message", "User deleted successfully"));
        } catch (UserService.UserNotFoundException e) {
            logger.warn("User not found for deletion: {}", username);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Failed to delete user: {} - {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "Failed to delete user: " + e.getMessage()
            ));
        }
    }

    private void validateSignupRequest(SignupRequest request) {
        logger.debug("Starting signup request validation");
        Map<String, String> validationErrors = new HashMap<>();
        
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            logger.debug("Username validation failed: empty username");
            validationErrors.put("username", "Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            logger.debug("Email validation failed: empty email");
            validationErrors.put("email", "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            logger.debug("Password validation failed: empty password");
            validationErrors.put("password", "Password is required");
        }
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            logger.debug("Role validation failed: empty role");
            validationErrors.put("role", "Role is required");
        }
    
        if (("CUSTOMER".equals(request.getRole()) || "COURIER".equals(request.getRole())) 
            && (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty())) {
            logger.debug("Phone number validation failed: required for customer/courier");
            validationErrors.put("phoneNumber", "Phone number is required for customers and couriers");
        }
        if ("COURIER".equals(request.getRole())) {
            if (request.getVehicleType() == null || request.getVehicleType().trim().isEmpty()) {
                logger.debug("Vehicle type validation failed: required for courier");
                validationErrors.put("vehicleType", "Vehicle type is required for couriers");
            } else if (!request.getVehicleType().matches("^(MOTORCYCLE|CAR|VAN)$")) {
                logger.debug("Vehicle type validation failed: invalid type: {}", request.getVehicleType());
                validationErrors.put("vehicleType", "Vehicle type must be MOTORCYCLE, CAR, or VAN");
            }
        }

        if (!validationErrors.isEmpty()) {
            logger.warn("Signup validation failed with errors: {}", validationErrors);
            throw new AuthenticationException.InvalidUserDataException("Invalid signup data: " + validationErrors);
        }
        
        logger.debug("Signup request validation completed successfully");
    }
}
