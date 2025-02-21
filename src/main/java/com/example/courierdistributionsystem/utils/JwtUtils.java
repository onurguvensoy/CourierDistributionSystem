package com.example.courierdistributionsystem.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.courierdistributionsystem.exception.AuthenticationException;

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
    
    // 1 hour in milliseconds
    private static final int JWT_EXPIRATION_MS = 3600000;
    
    // 5 minutes before expiration for refresh window
    private static final int REFRESH_WINDOW_MS = 300000;

    private final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();
    
    public String generateAuthToken(String username, String role, Long userId) {
        logger.debug("Generating JWT token for user: {}, role: {}", username, role);
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_MS);
            
            String token = Jwts.builder()
                    .setSubject(username)
                    .claim("role", role)
                    .claim("userId", userId)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
            
            logger.info("JWT token generated successfully for user: {}", username);
            logger.debug("Token expiration set to: {}", expiryDate);
            return token;
        } catch (Exception e) {
            logger.error("Failed to generate JWT token for user: {}", username, e);
            throw new AuthenticationException("Failed to generate token", e);
        }
    }
    
    public String getUsernameFromToken(String token) {
        logger.debug("Extracting username from token");
        try {
            String username = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            logger.debug("Username extracted from token: {}", username);
            return username;
        } catch (ExpiredJwtException e) {
            logger.warn("Attempt to extract username from expired token");
            throw new AuthenticationException.TokenExpiredException("Token has expired");
        } catch (Exception e) {
            logger.error("Failed to extract username from token", e);
            throw new AuthenticationException.InvalidTokenException("Invalid token");
        }
    }
    
    public String getRoleFromToken(String token) {
        logger.debug("Extracting role from token");
        try {
            String role = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", String.class);
            logger.debug("Role extracted from token: {}", role);
            return role;
        } catch (ExpiredJwtException e) {
            logger.warn("Attempt to extract role from expired token");
            throw new AuthenticationException.TokenExpiredException("Token has expired");
        } catch (Exception e) {
            logger.error("Failed to extract role from token", e);
            throw new AuthenticationException.InvalidTokenException("Invalid token");
        }
    }
    
    public Long getUserIdFromToken(String token) {
        logger.debug("Extracting userId from token");
        try {
            Long userId = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userId", Long.class);
            logger.debug("UserId extracted from token: {}", userId);
            return userId;
        } catch (ExpiredJwtException e) {
            logger.warn("Attempt to extract userId from expired token");
            throw new AuthenticationException.TokenExpiredException("Token has expired");
        } catch (Exception e) {
            logger.error("Failed to extract userId from token", e);
            throw new AuthenticationException.InvalidTokenException("Invalid token");
        }
    }
    
    public boolean validateToken(String token) {
        logger.debug("Validating JWT token");
        try {
            if (isTokenInvalidated(token)) {
                logger.warn("Token validation failed: Token is invalidated");
                throw new AuthenticationException.TokenInvalidatedException("Token has been invalidated");
            }

            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

            Date expiration = claims.getExpiration();
            logger.debug("Token expiration time: {}", expiration);

            if (shouldRefreshToken(expiration)) {
                logger.info("Token requires refresh, generating new token");
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                Long userId = claims.get("userId", Long.class);
                
                String newToken = generateAuthToken(username, role, userId);
                invalidateToken(token);
                logger.info("New token generated for user: {}", username);
                throw new TokenRefreshException(newToken);
            }
            
            logger.info("Token validation successful for user: {}", claims.getSubject());
            return true;
        } catch (TokenRefreshException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            logger.error("Token validation failed: Token has expired");
            invalidatedTokens.remove(token);
            throw new AuthenticationException.TokenExpiredException("Token has expired");
        } catch (SecurityException | MalformedJwtException e) {
            logger.error("Token validation failed: Invalid signature/token format", e);
            throw new AuthenticationException.InvalidTokenException("Invalid token");
        } catch (UnsupportedJwtException e) {
            logger.error("Token validation failed: Unsupported token format", e);
            throw new AuthenticationException.InvalidTokenException("Unsupported token format");
        } catch (IllegalArgumentException e) {
            logger.error("Token validation failed: Empty claims", e);
            throw new AuthenticationException.InvalidTokenException("Empty token claims");
        } catch (Exception e) {
            logger.error("Token validation failed: Unexpected error", e);
            throw new AuthenticationException.InvalidTokenException("Token validation failed");
        }
    }

    private boolean shouldRefreshToken(Date expiration) {
        boolean shouldRefresh = expiration.getTime() - System.currentTimeMillis() < REFRESH_WINDOW_MS;
        if (shouldRefresh) {
            logger.debug("Token is within refresh window. Expiration: {}, Current time: {}", 
                expiration, new Date());
        }
        return shouldRefresh;
    }

    public void invalidateToken(String token) {
        logger.debug("Attempting to invalidate token");
        try {
            if (token == null || token.trim().isEmpty()) {
                logger.error("Invalidation failed: Token is null or empty");
                throw new AuthenticationException.InvalidTokenException("Token cannot be null or empty");
            }

            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                logger.debug("Token validated before invalidation for user: {}", claims.getSubject());
            } catch (ExpiredJwtException e) {
                logger.warn("Cannot invalidate expired token");
                throw new AuthenticationException.TokenExpiredException("Cannot invalidate expired token");
            } catch (Exception e) {
                logger.error("Token validation failed during invalidation", e);
                throw new AuthenticationException.InvalidTokenException("Invalid token");
            }

            cleanupInvalidatedTokens();
            invalidatedTokens.add(token);
            logger.info("Token invalidated successfully");
            logger.debug("Current invalidated tokens count: {}", invalidatedTokens.size());
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during token invalidation", e);
            throw new AuthenticationException.LogoutFailedException("Failed to invalidate token", e);
        }
    }

    private boolean isTokenInvalidated(String token) {
        boolean isInvalidated = invalidatedTokens.contains(token);
        if (isInvalidated) {
            logger.debug("Token found in invalidated tokens list");
        }
        return isInvalidated;
    }

    private void cleanupInvalidatedTokens() {
        logger.debug("Starting cleanup of invalidated tokens. Current size: {}", invalidatedTokens.size());
        int beforeSize = invalidatedTokens.size();
        
        invalidatedTokens.removeIf(token -> {
            try {
                Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
                return false;
            } catch (ExpiredJwtException e) {
                logger.debug("Removing expired token from invalidated tokens list");
                return true;
            } catch (Exception e) {
                logger.warn("Error checking token expiration during cleanup: {}", e.getMessage());
                return false;
            }
        });
        
        int removedCount = beforeSize - invalidatedTokens.size();
        logger.info("Invalidated tokens cleanup completed. Removed {} expired tokens", removedCount);
    }
    
    private Key getSigningKey() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            logger.error("JWT secret key configuration error: Key length insufficient");
            throw new IllegalStateException("JWT secret key must be at least 256 bits (32 characters) long");
        }
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static class TokenRefreshException extends RuntimeException {
        private final String newToken;

        public TokenRefreshException(String newToken) {
            super("Token needs refresh");
            this.newToken = newToken;
        }

        public String getNewToken() {
            return newToken;
        }
    }
} 