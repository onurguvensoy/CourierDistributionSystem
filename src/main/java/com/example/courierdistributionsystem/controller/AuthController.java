package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.dto.LoginRequest;
import com.example.courierdistributionsystem.dto.SignupRequest;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest,
                                 HttpSession session) {
        try {
            Map<String, Object> response = authService.login(loginRequest.getUsername(), 
                                                           loginRequest.getPassword());

            if (response.containsKey("error")) {
                return ResponseEntity.badRequest().body(response);
            }

            // Store user information in session
            session.setAttribute("username", loginRequest.getUsername());
            session.setAttribute("role", response.get("role"));
            session.setAttribute("userId", response.get("userId"));
            session.setAttribute("email", response.get("email"));
            
            if (response.containsKey("phoneNumber")) {
                session.setAttribute("phoneNumber", response.get("phoneNumber"));
            }
            if (response.containsKey("isAvailable")) {
                session.setAttribute("isAvailable", response.get("isAvailable"));
            }
            if (response.containsKey("deliveryAddress")) {
                session.setAttribute("deliveryAddress", response.get("deliveryAddress"));
            }

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
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
            }
            
            authService.logoutUser();
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Logged out successfully"
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Authentication Error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during logout: {}", e.getMessage(), e);
            return ResponseEntity.ok().body(Map.of(
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
