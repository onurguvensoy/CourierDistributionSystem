package com.example.courierdistributionsystem.mapper;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.utils.JwtUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoginMapper {
    
    private final JwtUtils jwtUtils;

    public LoginMapper(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public Map<String, Object> toResponse(User user) {
        if (user == null) {
            return null;
        }

        Map<String, Object> response = new HashMap<>();
        String token = jwtUtils.generateAuthToken(
            user.getUsername(),
            user.getRole().toString(),
            user.getId()
        );

        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("userId", user.getId());
        response.put("email", user.getEmail());

        return response;
    }
} 