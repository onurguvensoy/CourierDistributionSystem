package com.example.courierdistributionsystem.exception;

public class CourierException extends RuntimeException {
    public CourierException(String message) {
        super(message);
    }
}

class CourierNotFoundException extends CourierException {
    public CourierNotFoundException(String message) {
        super(message);
    }
}

class CourierValidationException extends CourierException {
    public CourierValidationException(String message) {
        super(message);
    }
}

class CourierLocationException extends CourierException {
    public CourierLocationException(String message) {
        super(message);
    }
}

class CourierUpdateException extends CourierException {
    public CourierUpdateException(String message) {
        super(message);
    }
} 