package com.example.courierdistributionsystem.dto;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryHistoryDto {
    private Long id;
    private Long courierId;
    private String courierName;
    private Long packageId;
    private String trackingNumber;
    private LocalDateTime createdAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private DeliveryPackage.DeliveryStatus status;
    private String pickupLocation;
    private String deliveryLocation;
    private String deliveryNotes;
    private String customerFeedback;
    private Integer deliveryRating;
    private Map<String, Object> packageDetails;
    private Map<String, Object> customerDetails;
} 