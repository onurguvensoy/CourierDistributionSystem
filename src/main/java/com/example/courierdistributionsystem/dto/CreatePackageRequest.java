package com.example.courierdistributionsystem.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CreatePackageRequest {
    private String pickupAddress;
    private String deliveryAddress;
    private double weight;
    private String description;
    private String specialInstructions;
    private String recipientName;
    private String recipientPhone;
} 