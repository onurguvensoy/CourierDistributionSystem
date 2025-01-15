package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "packages")
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private User courier;

    @Column(unique = true, nullable = false)
    private String trackingNumber;

    @Column(nullable = false)
    private String deliveryAddress;

    private String pickupAddress;

    private String description;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private PackageStatus currentStatus = PackageStatus.CREATED;

    private boolean isActive = true;

    private LocalDateTime preferredDeliveryTime;

    private String deliveryTimeWindow;

    private LocalDateTime estimatedDeliveryTime;

    private String specialInstructions;

    @OneToMany(mappedBy = "deliveryPackage", cascade = CascadeType.ALL)
    private List<DeliveryReport> deliveryReports;

    @OneToMany(mappedBy = "deliveryPackage", cascade = CascadeType.ALL)
    private List<LocationHistory> locationHistory;

    @OneToMany(mappedBy = "deliveryPackage", cascade = CascadeType.ALL)
    private List<Rating> ratings;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        trackingNumber = generateTrackingNumber();
    }

    private String generateTrackingNumber() {
        return "TN" + System.currentTimeMillis();
    }

    public enum PackageStatus {
        CREATED,
        ASSIGNED,
        IN_TRANSIT,
        DELIVERED,
        FAILED,
        CANCELLED
    }
} 