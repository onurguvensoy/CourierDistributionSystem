package com.example.courierdistributionsystem.exception;

public class DeliveryReportException extends RuntimeException {
    public DeliveryReportException(String message) {
        super(message);
    }
}

class DeliveryReportNotFoundException extends DeliveryReportException {
    public DeliveryReportNotFoundException(String message) {
        super(message);
    }
}

class DeliveryReportValidationException extends DeliveryReportException {
    public DeliveryReportValidationException(String message) {
        super(message);
    }
}

class DeliveryReportUpdateException extends DeliveryReportException {
    public DeliveryReportUpdateException(String message) {
        super(message);
    }
} 