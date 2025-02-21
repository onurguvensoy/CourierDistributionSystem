package com.example.courierdistributionsystem.service.impl;

import com.example.courierdistributionsystem.dto.SignupDto;
import com.example.courierdistributionsystem.dto.LoginDto;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.service.IAuthService;
import com.example.courierdistributionsystem.service.IUserService;
import com.example.courierdistributionsystem.utils.JwtUtils;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Service
@Validated
public class AuthServiceImpl implements IAuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final IUserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthServiceImpl(IUserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Map<String, Object> login(LoginDto request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new AuthenticationException.InvalidUserDataException("Username and password are required");
        }

        User user = userService.findByUsername(request.getUsername())
            .orElseThrow(() -> new AuthenticationException.InvalidCredentialsException("Invalid username or password"));

        if (!request.getPassword().equals(user.getPassword())) {
            throw new AuthenticationException.InvalidCredentialsException("Invalid username or password");
        }

        String jwt = jwtUtils.generateAuthToken(request.getUsername(), user.getRole().toString(), user.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", request.getUsername());
        response.put("role", user.getRole());
        response.put("userId", user.getId());
        response.put("email", user.getEmail());

        return response;
    }

    @Override
    @Transactional
    public Map<String, String> logout(String token) {
        try {
            String username = jwtUtils.getUsernameFromToken(token);
            if (username == null) {
                throw new AuthenticationException.InvalidTokenException("Invalid token");
            }
            jwtUtils.invalidateToken(token);
            logger.info("User {} successfully logged out", username);
            return Map.of("status", "success", "message", "Successfully logged out");
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage());
            throw new RuntimeException("Failed to process logout: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Map<String, String> signup(SignupDto request) {
        validateSignupRequest(request);
        
        Map<String, String> signupData = new HashMap<>();
        signupData.put("username", request.getUsername());
        signupData.put("email", request.getEmail());
        signupData.put("password", request.getPassword());
        signupData.put("role", request.getRole());
        signupData.put("phoneNumber", request.getPhoneNumber());
        signupData.put("vehicleType", request.getVehicleType());

        return userService.signup(signupData);
    }

    private void validateSignupRequest(SignupDto request) {
        if (request == null) {
            throw new AuthenticationException.InvalidUserDataException("Signup request cannot be null");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new AuthenticationException.InvalidUserDataException("Password must be at least 8 characters long");
        }

        if (userService.existsByUsername(request.getUsername())) {
            throw new AuthenticationException.UserAlreadyExistsException("Username already exists");
        }

        if (userService.existsByEmail(request.getEmail())) {
            throw new AuthenticationException.UserAlreadyExistsException("Email already exists");
        }

        if (("CUSTOMER".equals(request.getRole()) || "COURIER".equals(request.getRole()))
            && (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty())) {
            throw new AuthenticationException.InvalidUserDataException("Phone number is required for customers and couriers");
        }

        if ("COURIER".equals(request.getRole())) {
            if (request.getVehicleType() == null || !request.getVehicleType().matches("^(MOTORCYCLE|CAR|VAN)$")) {
                throw new AuthenticationException.InvalidUserDataException("Vehicle type must be MOTORCYCLE, CAR, or VAN");
            }
        }
    }
} 