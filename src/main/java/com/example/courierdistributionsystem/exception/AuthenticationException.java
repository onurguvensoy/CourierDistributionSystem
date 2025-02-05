package com.example.courierdistributionsystem.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}

class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}

class UserAlreadyExistsException extends AuthenticationException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

class InvalidRoleException extends AuthenticationException {
    public InvalidRoleException(String message) {
        super(message);
    }
}

class MissingRequiredFieldException extends AuthenticationException {
    public MissingRequiredFieldException(String message) {
        super(message);
    }
} 