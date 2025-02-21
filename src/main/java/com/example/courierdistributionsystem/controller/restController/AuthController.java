package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.dto.LoginDto;
import com.example.courierdistributionsystem.dto.SignupDto;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import com.example.courierdistributionsystem.service.IAuthService;
import com.example.courierdistributionsystem.service.IUserService;
import com.example.courierdistributionsystem.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final IAuthService authService;
    private final IUserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(IAuthService authService, IUserService userService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginRequest) {
        try {
            Map<String, Object> response = authService.login(loginRequest);
            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.get("token"))
                .body(response);
        } catch (AuthenticationException.InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("status", "error", "message", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "An error occurred during login"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupDto request) {
        try {
            Map<String, String> response = authService.signup(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException.UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (AuthenticationException.InvalidUserDataException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "An error occurred during signup"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "status", "error",
                        "message", "Invalid token format",
                        "errorCode", "INVALID_TOKEN_FORMAT"
                    ));
            }
            String jwtToken = token.substring(7);
            Map<String, String> response = authService.logout(jwtToken);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException.InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "errorCode", e.getErrorCode()
                ));
        } catch (AuthenticationException.TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "errorCode", e.getErrorCode()
                ));
        } catch (AuthenticationException.LogoutFailedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "errorCode", e.getErrorCode()
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to process logout",
                    "errorCode", "INTERNAL_ERROR"
                ));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Invalid token format"));
            }
            
            String jwtToken = token.substring(7);
            
            try {
                jwtUtils.validateToken(jwtToken);
                return ResponseEntity.ok()
                    .body(Map.of("status", "success", "message", "Token is valid"));
            } catch (JwtUtils.TokenRefreshException e) {
                // Token is valid but needs refresh
                return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + e.getNewToken())
                    .body(Map.of(
                        "status", "success",
                        "message", "Token refreshed",
                        "token", e.getNewToken()
                    ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("status", "error", "message", "Invalid token"));
        }
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        try {
            userService.deleteByUsername(username);
            return ResponseEntity.ok()
                .body(Map.of("status", "success", "message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", "message", "Failed to delete user"));
        }
    }
}
