package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "delivery_package_id")
    private DeliveryPackage deliveryPackage;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    private User courier;

    private Double latitude;
    private Double longitude;
    private String locationDescription;
    private LocalDateTime timestamp;
    private String status;
    private String notes;
} 