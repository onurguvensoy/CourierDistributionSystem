package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.dto.LoginRequest;
import com.example.courierdistributionsystem.dto.SignupRequest;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.courierdistributionsystem.service.AuthService;
import com.example.courierdistributionsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest,
                                 HttpSession currentSession) {
        try {
            // Invalidate any existing session
            if (currentSession != null) {
                try {
                    currentSession.invalidate();
                } catch (IllegalStateException e) {
                    logger.debug("No existing session to invalidate");
                }
            }

            Map<String, Object> loginResponse = authService.login(loginRequest.getUsername(), 
                                                                loginRequest.getPassword());

            if (!Boolean.TRUE.equals(loginResponse.get("success"))) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Authentication Error",
                    "message", "Login failed"
                ));
            }

            // Create new session
            final HttpSession newSession = request.getSession(true);
            newSession.setMaxInactiveInterval(30 * 60); // 30 minutes timeout

            // Store user information in session
            loginResponse.forEach((key, value) -> {
                if (value != null) {
                    newSession.setAttribute(key, value);
                }
            });

            // Remove sensitive information before sending response
            loginResponse.remove("password");
            
            logger.info("New session created successfully for user: {}", loginResponse.get("username"));
            return ResponseEntity.ok(loginResponse);
            
        } catch (AuthenticationException e) {
            logger.warn("Authentication failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Authentication Error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal Server Error",
                "message", "An unexpected error occurred during login"
            ));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            Map<String, String> response = authService.signup(signupRequest);
            
            if (response.containsKey("error")) {
                return ResponseEntity.badRequest().body(response);
            }
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Authentication Error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during signup: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal Server Error",
                "message", "An unexpected error occurred during signup"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            if (username != null) {
                logger.info("Logging out user: {}", username);
                
                // Clear all session attributes
                session.removeAttribute("username");
                session.removeAttribute("role");
                session.removeAttribute("userId");
                session.removeAttribute("email");
                session.removeAttribute("phoneNumber");
                session.removeAttribute("isAvailable");
                session.removeAttribute("deliveryAddress");
                
                try {
                    session.invalidate();
                    logger.info("Session invalidated successfully for user: {}", username);
                } catch (IllegalStateException e) {
                    logger.debug("Session was already invalidated");
                }
            } else {
                logger.debug("No active session found for logout request");
            }
            
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", "Logged out successfully"
            ));
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage(), e);
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", "Logged out successfully"
            ));
        }
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        try {
            userService.deleteByUsername(username);
            return ResponseEntity.ok().body(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            logger.error("Failed to delete user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal Server Error",
                "message", "Failed to delete user: " + e.getMessage()
            ));
        }
    }
}
