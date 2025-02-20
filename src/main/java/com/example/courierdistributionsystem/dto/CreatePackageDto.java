package com.example.courierdistributionsystem.dto;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePackageDto {
    private String trackingNumber;
    
    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;
    
    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;
    
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String specialInstructions;
    private DeliveryPackage.DeliveryStatus status;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDestinationAddress() {
        return deliveryAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.deliveryAddress = destinationAddress;
    }
} 