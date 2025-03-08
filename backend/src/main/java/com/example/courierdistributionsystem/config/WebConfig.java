package com.example.courierdistributionsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import com.example.courierdistributionsystem.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization", 
                          "Access-Control-Allow-Origin", "Access-Control-Allow-Headers",
                          "Access-Control-Request-Method", "Access-Control-Request-Headers",
                          "X-Requested-With")
            .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Authorization")
            .allowCredentials(true)
            .maxAge(3600);
    }

    @Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/api/auth/login", "/api/auth/signup", "/h2-console/**", "/ws/**", "/ws/*", "/ws/*/**"); // Allow all WebSocket paths
}
} 