package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.SignupRequest;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Validated
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserService userService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public Map<String, Object> login(String username, String password) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found after authentication");
            }
            User user = userOpt.get();
            String jwt = jwtUtils.generateToken(username, user.getRole().toString(), user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("username", username);
            response.put("role", user.getRole());
            response.put("userId", user.getId());

            logger.info("User {} successfully logged in", username);
            return response;
        } catch (BadCredentialsException e) {
            logger.error("Authentication failed for user: {} - Invalid credentials", username);
            throw e;
        }
    }

    @Transactional
    public Map<String, String> signup(SignupRequest request) {
        logger.debug("Processing signup request for user: {}", request.getUsername());
        
        try {
            validateSignupRequest(request);
            return processSignup(request);
        } catch (AuthenticationException e) {
            logger.warn("Signup failed for user: {} - {}", request.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during signup for user: {} - {}", request.getUsername(), e.getMessage(), e);
            throw new AuthenticationException("An unexpected error occurred during signup", "SIGNUP_ERROR");
        }
    }

    private void validateSignupRequest(SignupRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            logger.warn("Username already exists: {}", request.getUsername());
            throw new AuthenticationException.UserAlreadyExistsException("Username already exists");
        }

        if (userService.existsByEmail(request.getEmail())) {
            logger.warn("Email already exists: {}", request.getEmail());
            throw new AuthenticationException.UserAlreadyExistsException("Email already exists");
        }
    }

    private Map<String, String> processSignup(SignupRequest request) {
        Map<String, String> signupData = new HashMap<>();
        signupData.put("username", request.getUsername());
        signupData.put("email", request.getEmail());
        signupData.put("password", request.getPassword());
        signupData.put("role", request.getRole());
        signupData.put("phoneNumber", request.getPhoneNumber());
        signupData.put("vehicleType", request.getVehicleType());

        Map<String, String> response = userService.signup(signupData);
        logger.info("User {} successfully signed up", request.getUsername());
        return response;
    }
}
