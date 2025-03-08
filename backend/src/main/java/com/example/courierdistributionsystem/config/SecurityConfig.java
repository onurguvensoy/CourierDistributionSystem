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

            .csrf(csrf -> csrf.disable())
            

            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            

            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                

                .requestMatchers("/api/auth/**").permitAll()
                

                .requestMatchers("/ws/**", "/topic/**", "/app/**", "/user/**", "/queue/**").permitAll()
                

                .requestMatchers("/h2-console/**").permitAll()
                

                .anyRequest().permitAll()
            )
            

            .headers(headers -> headers
                .frameOptions().disable()
                .xssProtection().disable()
            );

        return http.build();
    }
} 