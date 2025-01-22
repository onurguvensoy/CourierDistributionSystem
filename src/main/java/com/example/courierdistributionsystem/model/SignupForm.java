package com.example.courierdistributionsystem.model;

import lombok.Data;

@Data
public class SignupForm {
    private String username;
    private String email;
    private String password;
    private String roleType;
    private String deliveryAddress;
    private String billingAddress;
    private String phoneNumber;
    private String vehicleType;
} 