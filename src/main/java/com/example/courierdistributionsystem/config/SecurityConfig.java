package com.example.courierdistributionsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsFilter corsFilter;

    public SecurityConfig(CorsFilter corsFilter) {
        this.corsFilter = corsFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF as we're using JWT
            .csrf(csrf -> csrf.disable())
            
            // Add CORS filter
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Set session management to stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow OPTIONS requests for CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Allow authentication endpoints
                .requestMatchers("/api/auth/**").permitAll()
                
                // Allow WebSocket endpoints
                .requestMatchers("/ws/**", "/topic/**", "/app/**", "/user/**", "/queue/**").permitAll()
                
                // Allow H2 console for development
                .requestMatchers("/h2-console/**").permitAll()
                
                // Require authentication for all other requests
                .anyRequest().permitAll()
            )
            
            // Configure headers
            .headers(headers -> headers
                .frameOptions().disable()
                .xssProtection().disable()
            );

        return http.build();
    }
} 