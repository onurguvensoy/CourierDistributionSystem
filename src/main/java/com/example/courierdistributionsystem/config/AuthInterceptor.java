package com.example.courierdistributionsystem.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Arrays;
import java.util.List;
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/",
        "/auth/login",
        "/auth/signup",
        "/api/auth/login",
        "/api/auth/signup",
        "/css/",
        "/js/",
        "/images/",
        "/webjars/",
        "/h2-console"
    );

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                           @NonNull HttpServletResponse response, 
                           @NonNull Object handler) throws Exception {
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            return true;
        }
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            return true;
        }

        if (path.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        response.sendRedirect("/auth/login");
        return false;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(publicPath -> 
            path.equals(publicPath) || path.startsWith(publicPath));
    }
} 