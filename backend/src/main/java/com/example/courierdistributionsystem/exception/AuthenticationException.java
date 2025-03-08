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

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static class InvalidCredentialsException extends AuthenticationException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public static class LoginFailedException extends AuthenticationException {
        public LoginFailedException(String message) {
            super(message);
        }

        public LoginFailedException(String message, Throwable cause) {
            super(message, cause);
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

    public static class InvalidTokenException extends AuthenticationException {
        public InvalidTokenException(String message) {
            super(message, "INVALID_TOKEN");
        }
    }

    public static class TokenExpiredException extends AuthenticationException {
        public TokenExpiredException(String message) {
            super(message, "TOKEN_EXPIRED");
        }
    }

    public static class LogoutFailedException extends AuthenticationException {
        public LogoutFailedException(String message) {
            super(message, "LOGOUT_FAILED");
        }

        public LogoutFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class TokenInvalidatedException extends AuthenticationException {
        public TokenInvalidatedException(String message) {
            super(message, "TOKEN_INVALIDATED");
        }
    }
} 