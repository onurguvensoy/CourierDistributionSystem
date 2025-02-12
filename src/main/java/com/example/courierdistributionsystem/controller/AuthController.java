package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.dto.LoginRequest;
import com.example.courierdistributionsystem.dto.SignupRequest;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import com.example.courierdistributionsystem.service.AuthService;
import com.example.courierdistributionsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
@CrossOrigin(
    origins = {"http://localhost:8080"},
    allowedHeaders = {"Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"},
    exposedHeaders = {"Access-Control-Allow-Origin"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE},
    allowCredentials = "true",
    maxAge = 3600
)
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserService userService;
    private final HttpServletRequest request;

    @Autowired
    public AuthController(AuthService authService, UserService userService, HttpServletRequest request) {
        this.authService = authService;
        this.userService = userService;
        this.request = request;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpSession currentSession) {
        String username = loginRequest.getUsername();
        logger.info("Login attempt for user: {}", username);
        
        try {
            logger.debug("Received login request - Username: {}", username);
            if (username == null || username.trim().isEmpty() || 
                loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                logger.warn("Invalid login request: Missing credentials");
                return ResponseEntity.status(401).body(Map.of(
                    "error", "INVALID_CREDENTIALS",
                    "message", "Username and password are required"
                ));
            }
            if (currentSession != null && !currentSession.isNew()) {
                logger.debug("Invalidating existing session for user: {}", username);
                try {
                    currentSession.invalidate();
                } catch (IllegalStateException e) {
                    logger.warn("Failed to invalidate existing session: {}", e.getMessage());
                }
            }

            Map<String, Object> loginResponse = authService.login(username, loginRequest.getPassword());
            HttpSession newSession = request.getSession(true);
            newSession.setMaxInactiveInterval(30 * 60);
            loginResponse.forEach((key, value) -> {
                if (value != null) {
                    newSession.setAttribute(key, value);
                    logger.debug("Setting session attribute: {} for user: {}", key, username);
                }
            });
            loginResponse.remove("password");
            
            logger.info("Login successful for user: {}", username);
            return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "http://localhost:8080")
                .header("Access-Control-Allow-Credentials", "true")
                .body(loginResponse);
            
        } catch (AuthenticationException.InvalidCredentialsException e) {
            logger.warn("Invalid credentials for user: {} - {}", username, e.getMessage());
            return ResponseEntity.status(401)
                .header("Access-Control-Allow-Origin", "http://localhost:8080")
                .header("Access-Control-Allow-Credentials", "true")
                .body(Map.of(
                    "error", "INVALID_CREDENTIALS",
                    "message", "Invalid username or password"
                ));
        } catch (AuthenticationException e) {
            logger.error("Authentication error for user: {} - {}", username, e.getMessage());
            return ResponseEntity.status(401)
                .header("Access-Control-Allow-Origin", "http://localhost:8080")
                .header("Access-Control-Allow-Credentials", "true")
                .body(Map.of(
                    "error", e.getErrorCode(),
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            logger.error("Unexpected error during login for user: {} - {}", username, e.getMessage(), e);
            return ResponseEntity.status(500)
                .header("Access-Control-Allow-Origin", "http://localhost:8080")
                .header("Access-Control-Allow-Credentials", "true")
                .body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred during login"
                ));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        String username = signupRequest.getUsername();
        logger.info("Processing signup request for username: {}", username);
        
        try {
            validateSignupRequest(signupRequest);

            Map<String, String> response = authService.signup(signupRequest);
            
            if (response.containsKey("error")) {
                logger.warn("Signup failed for user: {} - {}", username, response.get("error"));
                throw new AuthenticationException.InvalidUserDataException(response.get("error"));
            }
            
            logger.info("Signup successful for user: {}", username);
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException.UserAlreadyExistsException e) {
            logger.warn("User already exists: {} - {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getErrorCode(),
                "message", e.getMessage()
            ));
        } catch (AuthenticationException.InvalidUserDataException e) {
            logger.warn("Invalid user data for: {} - {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getErrorCode(),
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during signup for user: {} - {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "An unexpected error occurred during signup"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.info("Processing logout request for user: {}", username);
        
        try {
            if (username != null) {
                String[] attributes = {"username", "role", "userId", "email", "phoneNumber", 
                                    "isAvailable", "deliveryAddress"};
                
                for (String attribute : attributes) {
                    session.removeAttribute(attribute);
                    logger.debug("Removed session attribute: {} for user: {}", attribute, username);
                }
                
                try {
                    session.invalidate();
                    logger.info("Session invalidated successfully for user: {}", username);
                } catch (IllegalStateException e) {
                    logger.warn("Session was already invalidated for user: {}", username);
                }
            } else {
                logger.debug("No active session found for logout request");
            }
            
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", "Logged out successfully"
            ));
        } catch (Exception e) {
            logger.error("Error during logout for user: {} - {}", username, e.getMessage(), e);
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", "Logged out successfully"
            ));
        }
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
        Map<String, String> validationErrors = new HashMap<>();
        
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            validationErrors.put("username", "Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            validationErrors.put("email", "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            validationErrors.put("password", "Password is required");
        }
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            validationErrors.put("role", "Role is required");
        }
    
        if (("CUSTOMER".equals(request.getRole()) || "COURIER".equals(request.getRole())) 
            && (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty())) {
            validationErrors.put("phoneNumber", "Phone number is required for customers and couriers");
        }
        if ("COURIER".equals(request.getRole())) {
            if (request.getVehicleType() == null || request.getVehicleType().trim().isEmpty()) {
                validationErrors.put("vehicleType", "Vehicle type is required for couriers");
            } else if (!request.getVehicleType().matches("^(MOTORCYCLE|CAR|VAN)$")) {
                validationErrors.put("vehicleType", "Vehicle type must be MOTORCYCLE, CAR, or VAN");
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new AuthenticationException.InvalidUserDataException("Invalid signup data: " + validationErrors);
        }
    }
}
