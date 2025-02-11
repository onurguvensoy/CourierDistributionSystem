package com.example.courierdistributionsystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePackageRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Pickup address is required")
    @Size(min = 5, max = 200, message = "Pickup address must be between 5 and 200 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s,.-]+$", message = "Pickup address can only contain letters, numbers, spaces, and basic punctuation")
    private String pickupAddress;

    @NotBlank(message = "Delivery address is required")
    @Size(min = 5, max = 200, message = "Delivery address must be between 5 and 200 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s,.-]+$", message = "Delivery address can only contain letters, numbers, spaces, and basic punctuation")
    private String deliveryAddress;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
    @DecimalMax(value = "1000.0", message = "Weight cannot exceed 1000 kg")
    private double weight;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @Size(max = 200, message = "Special instructions cannot exceed 200 characters")
    private String specialInstructions;
} 