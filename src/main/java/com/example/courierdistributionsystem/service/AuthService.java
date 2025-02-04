package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private PasswordEncoderService passwordEncoder;

    @Autowired
    private HttpSession httpSession;

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> response = new HashMap<>();

        Optional<? extends User> userOpt = userManagementService.findByUsername(username);
        if (userOpt.isEmpty()) {
            response.put("error", "Invalid username or password");
            return response;
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("error", "Invalid username or password");
            return response;
        }

        // Set session attributes
        httpSession.setAttribute("username", username);
        httpSession.setAttribute("role", user.getRole());
        httpSession.setAttribute("userId", user.getId());
        httpSession.setAttribute("email", user.getEmail());

        // Add user-specific information to response
        response.put("username", username);
        response.put("role", user.getRole());
        response.put("userId", user.getId());
        response.put("email", user.getEmail());

        if (user instanceof Courier) {
            Courier courier = (Courier) user;
            response.put("phoneNumber", courier.getPhoneNumber());
            response.put("isAvailable", courier.isAvailable());
            response.put("currentZone", courier.getCurrentZone());
        } else if (user instanceof Customer) {
            Customer customer = (Customer) user;
            response.put("phoneNumber", customer.getPhoneNumber());
        }

        return response;
    }

    public Map<String, String> signup(Map<String, String> signupData) {
        Map<String, String> response = new HashMap<>();
        
        String username = signupData.get("username");
        String email = signupData.get("email");
        String password = signupData.get("password");
        String roleType = signupData.get("roleType");
        String phoneNumber = signupData.get("phoneNumber");

        // Validate required fields
        if (username == null || email == null || password == null || roleType == null) {
            response.put("error", "Missing required fields");
            return response;
        }

        // Check if username or email already exists
        if (userManagementService.existsByUsername(username)) {
            response.put("error", "Username already exists");
            return response;
        }

        if (userManagementService.existsByEmail(email)) {
            response.put("error", "Email already exists");
            return response;
        }

        try {
            String hashedPassword = passwordEncoder.encode(password);

            switch (roleType.toUpperCase()) {
                case "CUSTOMER" -> {
                    Customer customer = Customer.builder()
                            .username(username)
                            .email(email)
                            .password(hashedPassword)
                            .role(User.UserRole.CUSTOMER)
                            .phoneNumber(phoneNumber)
                            .build();
                    userManagementService.saveCustomer(customer);
                }
                case "COURIER" -> {
                    Courier courier = Courier.builder()
                            .username(username)
                            .email(email)
                            .password(hashedPassword)
                            .role(User.UserRole.COURIER)
                            .phoneNumber(phoneNumber)
                            .available(true)
                            .build();
                    userManagementService.saveCourier(courier);
                }
                default -> {
                    response.put("error", "Invalid role type");
                    return response;
                }
            }

            response.put("message", "Registration successful");
            return response;

        } catch (Exception e) {
            response.put("error", "Error during registration: " + e.getMessage());
            return response;
        }
    }

    public void logoutUser() {
        httpSession.invalidate();
    }
}
