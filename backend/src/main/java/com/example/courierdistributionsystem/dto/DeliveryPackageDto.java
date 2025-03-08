package com.example.courierdistributionsystem.dto;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPackageDto {
    private Long id;
    
    private String trackingNumber;
    
    private String customerUsername;
    
    private String courierUsername;
    
    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;
    
    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;
    
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;
    private String description;
    private String specialInstructions;
    private DeliveryPackage.DeliveryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> customerDetails;
    private Map<String, Object> courierDetails;
    public DeliveryPackageDto(String pickupAddress, String deliveryAddress, Double weight, 
                             String description, String specialInstructions) {
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.weight = weight;
        this.description = description;
        this.specialInstructions = specialInstructions;
    }
} 