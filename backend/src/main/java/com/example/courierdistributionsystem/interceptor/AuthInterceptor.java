package com.example.courierdistributionsystem.interceptor;

import com.example.courierdistributionsystem.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/signup",
        "/h2-console",
        "/ws"
    );

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        
        String path = request.getRequestURI();
        
        // Allow public endpoints
        if (isPublicEndpoint(path)) {
            return true;
        }
        
        // Check JWT token for protected endpoints
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.validateToken(token);
        }
        
        return false;
    }
    
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream()
            .anyMatch(endpoint -> path.startsWith(endpoint));
    }
} 