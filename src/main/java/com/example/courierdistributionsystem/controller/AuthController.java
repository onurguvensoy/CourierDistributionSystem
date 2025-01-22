package com.example.courierdistributionsystem.controller;
import com.example.courierdistributionsystem.model.SignupForm;
import com.example.courierdistributionsystem.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> signupRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract required fields
            String username = signupRequest.get("username");
            String email = signupRequest.get("email");
            String password = signupRequest.get("password");
            String roleType = signupRequest.get("roleType");
            
            // Validate required fields
            if (username == null || email == null || password == null || roleType == null) {
                response.put("status", "error");
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }

            authService.PostSignup(username, email, password, roleType);
            
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
            Map<String, String> authResponse = authService.CheckLoginUser(Map.of(
                "username", loginRequest.get("username"),
                "password", loginRequest.get("password")
            ));

            if (authResponse.containsKey("error")) {
                response.put("status", "error");
                response.put("message", authResponse.get("error"));
                return ResponseEntity.badRequest().body(response);
            }

            session.setAttribute("username", authResponse.get("username"));
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

    // Web view endpoints
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                              @RequestParam(required = false) String registered,
                              @RequestParam(required = false) String logout,
                              Model model,
                              HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/dashboard";
        }

        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (registered != null) {
            model.addAttribute("message", "Registration successful! Please login");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model, HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }
}


