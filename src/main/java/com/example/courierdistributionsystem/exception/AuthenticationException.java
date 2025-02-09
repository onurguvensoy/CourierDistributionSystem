package com.example.courierdistributionsystem.exception;

public class AuthenticationException extends RuntimeException {
    private final String errorCode;

    public AuthenticationException(String message) {
        super(message);
        this.errorCode = "AUTH_ERROR";
    }

    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static class InvalidCredentialsException extends AuthenticationException {
        public InvalidCredentialsException() {
            super("Invalid username or password", "INVALID_CREDENTIALS");
        }
    }

    public static class UserAlreadyExistsException extends AuthenticationException {
        public UserAlreadyExistsException(String message) {
            super(message, "USER_EXISTS");
        }
    }

    public static class InvalidUserDataException extends AuthenticationException {
        public InvalidUserDataException(String message) {
            super(message, "INVALID_USER_DATA");
        }
    }

    public static class SessionException extends AuthenticationException {
        public SessionException(String message) {
            super(message, "SESSION_ERROR");
        }
    }
} 