package com.example.courierdistributionsystem.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePackageRequest {
    private String username;
    private String pickupAddress;
    private String deliveryAddress;
    private double weight;
    private String description;
    private String specialInstructions;
} 