package com.example.courierdistributionsystem.model.role;

public class RoleFactory {
    public static Role createRole(String roleType) {
        return switch (roleType.toUpperCase()) {
            case "ADMIN" -> new AdminRole();
            case "CUSTOMER" -> new CustomerRole();
            case "COURIER" -> new CourierRole();
            default -> throw new IllegalArgumentException("Unknown role type: " + roleType);
        };
    }
} 