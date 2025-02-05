package com.example.courierdistributionsystem.exception;

public class CustomerException extends RuntimeException {
    public CustomerException(String message) {
        super(message);
    }
}

class CustomerNotFoundException extends CustomerException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}

class CustomerValidationException extends CustomerException {
    public CustomerValidationException(String message) {
        super(message);
    }
}

class CustomerUpdateException extends CustomerException {
    public CustomerUpdateException(String message) {
        super(message);
    }
} 