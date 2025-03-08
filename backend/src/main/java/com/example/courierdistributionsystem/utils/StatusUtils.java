package com.example.courierdistributionsystem.utils;

public class StatusUtils {
    public static String getStatusBadgeClass(String status) {
        if (status == null) return "secondary";
        
        switch(status.toUpperCase()) {
            case "PENDING": return "warning";
            case "ASSIGNED": return "info";
            case "PICKED_UP": return "primary";
            case "IN_TRANSIT": return "info";
            case "DELIVERED": return "success";
            case "CANCELLED": return "danger";
            default: return "secondary";
        }
    }
} 