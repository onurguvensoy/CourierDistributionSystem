package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> signupRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = signupRequest.get("username");
            String email = signupRequest.get("email");
            String password = signupRequest.get("password");
            String roleType = signupRequest.get("roleType");
            
            if (username == null || email == null || password == null || roleType == null) {
                response.put("status", "error");
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, String> signupResponse = authService.signup(signupRequest);
            
            if (signupResponse.containsKey("error")) {
                response.put("status", "error");
                response.put("message", signupResponse.get("error"));
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("status", "success");
            response.put("message", "User registered successfully");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest,
                                 HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> authResponse = authService.login(
                loginRequest.get("username"),
                loginRequest.get("password")
            );

            if (authResponse.containsKey("error")) {
                response.put("status", "error");
                response.put("message", authResponse.get("error"));
                return ResponseEntity.badRequest().body(response);
            }

            session.setAttribute("username", loginRequest.get("username"));
            session.setAttribute("role", authResponse.get("role"));
            if (authResponse.containsKey("phoneNumber")) {
                session.setAttribute("phoneNumber", authResponse.get("phoneNumber"));
            }
            if (authResponse.containsKey("isAvailable")) {
                session.setAttribute("isAvailable", authResponse.get("isAvailable"));
            }

            response.put("status", "success");
            response.put("message", "Login successful");
            response.put("user", authResponse);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            session.invalidate();
            response.put("status", "success");
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred during logout");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}


