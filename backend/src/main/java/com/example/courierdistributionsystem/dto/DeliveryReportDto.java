package com.example.courierdistributionsystem.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryReportDto {
    private Long id;
    
    @NotNull(message = "Package ID is required")
    private Long packageId;
    
    private String trackingNumber;
    
    private String courierUsername;
    private String customerUsername;
    
    @NotNull(message = "Delivery time is required")
    private LocalDateTime deliveryTime;
    
    private String deliveryNotes;
    private String customerSignature;
    private String deliveryProofPhoto;
    private Map<String, Object> additionalDetails;
    private LocalDateTime createdAt;
    private LocalDateTime completionTime;
    private Double distanceTraveled;
} 