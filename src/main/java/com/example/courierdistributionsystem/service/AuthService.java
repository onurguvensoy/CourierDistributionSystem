package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.Admin;
import com.example.courierdistributionsystem.repository.CustomerRepository;
import com.example.courierdistributionsystem.repository.CourierRepository;
import com.example.courierdistributionsystem.repository.AdminRepository;
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
    private CustomerRepository customerRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private HttpSession httpSession;

    public Map<String, String> signup(Map<String, String> signupData) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String username = signupData.get("username");
            String email = signupData.get("email");
            String password = signupData.get("password");
            String roleType = signupData.get("roleType");
            String phoneNumber = signupData.get("phoneNumber");
            String deliveryAddress = signupData.get("deliveryAddress");

            if (username == null || email == null || password == null || roleType == null) {
                response.put("error", "Missing required fields");
                return response;
            }

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

            if (roleType.equals("CUSTOMER")) {
                Customer customer = Customer.builder()
                        .username(username)
                        .email(email)
                        .password(passwordEncoder.hashPassword(password))
                        .role(User.UserRole.CUSTOMER)
                        .deliveryAddress(deliveryAddress)
                        .phoneNumber(phoneNumber)
                        .averageRating(0.0)
                        .build();
                customerRepository.save(customer);
            } else if (roleType.equals("COURIER")) {
                Courier courier = Courier.builder()
                        .username(username)
                        .email(email)
                        .password(passwordEncoder.hashPassword(password))
                        .role(User.UserRole.COURIER)
                        .phoneNumber(phoneNumber)
                        .available(true)
                        .build();
                courierRepository.save(courier);
            } else {
                Admin admin = Admin.builder()
                        .username(username)
                        .email(email)
                        .password(passwordEncoder.hashPassword(password))
                        .role(User.UserRole.ADMIN)
                        .build();
                adminRepository.save(admin);
            }

            response.put("success", "User registered successfully");
            return response;

        } catch (Exception e) {
            response.put("error", "An error occurred during registration: " + e.getMessage());
            return response;
        }
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = null;
            
            // Try to find user in each repository
            Customer customer = customerRepository.findByUsername(username).orElse(null);
            if (customer != null) {
                user = customer;
            } else {
                Courier courier = courierRepository.findByUsername(username).orElse(null);
                if (courier != null) {
                    user = courier;
                } else {
                    Admin admin = adminRepository.findByUsername(username).orElse(null);
                    if (admin != null) {
                        user = admin;
                    }
                }
            }

            if (user == null) {
                response.put("error", "User not found");
                return response;
            }

            String hashedPassword = passwordEncoder.hashPassword(password);
            if (!user.getPassword().equals(hashedPassword)) {
                response.put("error", "Invalid password");
                return response;
            }

            httpSession.setAttribute("username", username);
            httpSession.setAttribute("role", user.getRole().toString());

            response.put("success", true);
            response.put("role", user.getRole().toString());
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
        return customerRepository.findByUsername(username).isPresent() ||
               courierRepository.findByUsername(username).isPresent() ||
               adminRepository.findByUsername(username).isPresent();
    }

    public boolean isEmailExists(String email) {
        return customerRepository.findByEmail(email).isPresent() ||
               courierRepository.findByEmail(email).isPresent() ||
               adminRepository.findByEmail(email).isPresent();
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

        if (roleType.equals("CUSTOMER")) {
            Customer customer = Customer.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.hashPassword(password))
                    .role(User.UserRole.CUSTOMER)
                    .createdAt(LocalDateTime.now())
                    .averageRating(0.0)
                    .build();
            customerRepository.save(customer);
        } else if (roleType.equals("COURIER")) {
            Courier courier = Courier.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.hashPassword(password))
                    .role(User.UserRole.COURIER)
                    .createdAt(LocalDateTime.now())
                    .averageRating(0.0)
                    .build();
            courierRepository.save(courier);
        } else {
            Admin admin = Admin.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.hashPassword(password))
                    .role(User.UserRole.ADMIN)
                    .build();
            adminRepository.save(admin);
        }
    }
}
