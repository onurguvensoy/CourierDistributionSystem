package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "delivery_package_status")
public class PackageStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "delivery_package_id", nullable = false)
    private DeliveryPackage deliveryPackage;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    private User courier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryPackage.DeliveryStatus status;

    @Column(columnDefinition = "DOUBLE")
    private Double latitude;

    @Column(columnDefinition = "DOUBLE")
    private Double longitude;

    private LocalDateTime createdAt;

    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 