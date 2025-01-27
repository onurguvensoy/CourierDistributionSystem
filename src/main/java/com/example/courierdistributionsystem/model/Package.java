package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    private User courier;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private String deliveryAddress;

    private String description;

    private Double weight;

    @Enumerated(EnumType.STRING)
    private PackageStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    private Double currentLatitude;
    private Double currentLongitude;
    private String currentLocation;

    private String specialInstructions;

    public enum PackageStatus {
        PENDING,
        ASSIGNED,
        PICKED_UP,
        IN_TRANSIT,
        DELIVERED,
        CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = PackageStatus.PENDING;
        }
    }

    public PackageStatus getCurrentStatus() {
        return this.status;
    }

    public void setCurrentStatus(PackageStatus status) {
        this.status = status;
    }
} 