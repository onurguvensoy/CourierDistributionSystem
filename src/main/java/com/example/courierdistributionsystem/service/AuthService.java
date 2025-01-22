package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.repository.UserRepository;
import com.example.courierdistributionsystem.utils.PasswordEncoder;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpSession httpSession;

    public Map<String, String> CheckSignup(Map<String, String> signupData) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String username = signupData.get("username");
            String email = signupData.get("email");
            String password = signupData.get("password");
            String roleType = signupData.get("roleType");
            String phoneNumber = signupData.get("phoneNumber");
            String deliveryAddress = signupData.get("deliveryAddress");
            String vehicleType = signupData.get("vehicleType");
            
            if (username == null || email == null || password == null || roleType == null) {
                response.put("error", "Missing required fields");
                return response;
            }

            // Role-specific validation
            if (roleType.equals("CUSTOMER")) {
                if (phoneNumber == null || deliveryAddress == null) {
                    response.put("error", "Phone number and delivery address are required for customers");
                    return response;
                }
            } else if (roleType.equals("COURIER")) {
                if (phoneNumber == null) {
                    response.put("error", "Phone number is required for couriers");
                    return response;
                }
            }

            if (isUsernameExists(username)) {
                response.put("error", "Username already exists");
                return response;
            }

            if (isEmailExists(email)) {
                response.put("error", "Email already exists");
                return response;
            }

            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.hashPassword(password))
                    .role(User.UserRole.valueOf(roleType))
                    .createdAt(LocalDateTime.now())
                    .build();

            if (roleType.equals("CUSTOMER")) {
                Customer customer = Customer.builder()
                        .user(user)
                        .deliveryAddress(deliveryAddress)
                        .phoneNumber(phoneNumber)
                        .createdAt(LocalDateTime.now())
                        .averageRating(0.0)
                        .build();
                user.setCustomer(customer);
            } else if (roleType.equals("COURIER")) {
                Courier courier = Courier.builder()
                        .user(user)
                        .phoneNumber(phoneNumber)
                        .isAvailable(true)
                        .build();
                user.setCourier(courier);
            }

            userRepository.save(user);
            response.put("success", "User registered successfully");
            return response;

        } catch (Exception e) {
            response.put("error", "An error occurred during registration: " + e.getMessage());
            return response;
        }
    }

    public Map<String, String> CheckLoginUser(Map<String, String> credentials) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            if (username == null || password == null) {
                response.put("error", "Username and password are required");
                return response;
            }

            User user = userRepository.findByUsername(username)
                    .orElse(null);

            if (user == null) {
                response.put("error", "Invalid username or password");
                return response;
            }

            String hashedPassword = passwordEncoder.hashPassword(password);
            if (!user.getPassword().equals(hashedPassword)) {
                response.put("error", "Invalid username or password");
                return response;
            }

            response.put("success", "Login successful");
            response.put("username", user.getUsername());
            response.put("role", user.getRole().name());

            if (user.getRole() == User.UserRole.CUSTOMER && user.getCustomer() != null) {
                response.put("phoneNumber", user.getCustomer().getPhoneNumber());
            } else if (user.getRole() == User.UserRole.COURIER && user.getCourier() != null) {
                response.put("phoneNumber", user.getCourier().getPhoneNumber());
                response.put("isAvailable", String.valueOf(user.getCourier().isAvailable()));
            }
            
            return response;

        } catch (Exception e) {
            response.put("error", "An error occurred during login: " + e.getMessage());
            return response;
        }
    }

    public void logoutUser() {
        httpSession.invalidate();
    }

    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public void PostSignup(String username, String email, String password, String roleType) {

        if (username == null || email == null || password == null || roleType == null) {
            throw new IllegalArgumentException("Missing required fields");
        }

        if (isUsernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (isEmailExists(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.hashPassword(password))
                .role(User.UserRole.valueOf(roleType))
                .createdAt(LocalDateTime.now())
                .build();

        if (roleType.equals("CUSTOMER")) {
            Customer customer = Customer.builder()
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .averageRating(0.0)
                    .build();
            user.setCustomer(customer);
        } else if (roleType.equals("COURIER")) {
            Courier courier = Courier.builder()
                    .user(user)
                    .isAvailable(true)
                    .build();
            user.setCourier(courier);
        }

        userRepository.save(user);
    }
}
