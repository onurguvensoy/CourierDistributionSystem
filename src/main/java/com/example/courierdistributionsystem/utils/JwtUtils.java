package com.example.courierdistributionsystem.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("2520000")
    private int jwtExpirationMs;

    // Store invalidated tokens until they expire
    private final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();


    
    public String generateAuthToken(String username, String role, Long userId) {

        Claims generalClaims = Jwts.claims().setIssuer(username).setSubject(role).setAudience(userId.toString());
        return Jwts.builder()
                .setClaims(generalClaims)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    public String getRoleFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }
    
    public Long getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }
    
    public boolean validateToken(String token) {
        try {
            if (isTokenInvalidated(token)) {
                logger.warn("Token is invalidated");
                return false;
            }

            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            invalidatedTokens.remove(token);
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public void invalidateToken(String username) {
        try {
            // Clean up expired tokens
            cleanupInvalidatedTokens();
            
            // Add current token to invalidated set
            invalidatedTokens.add(username);
            logger.info("Token invalidated for user: {}", username);
        } catch (Exception e) {
            logger.error("Error invalidating token for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to invalidate token", e);
        }
    }

    private boolean isTokenInvalidated(String token) {
        return invalidatedTokens.contains(token);
    }

    private void cleanupInvalidatedTokens() {
        invalidatedTokens.removeIf(token -> {
            try {
                Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
                return false;
            } catch (ExpiredJwtException e) {
                return true;
            } catch (Exception e) {
                logger.warn("Error checking token expiration: {}", e.getMessage());
                return false;
            }
        });
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 