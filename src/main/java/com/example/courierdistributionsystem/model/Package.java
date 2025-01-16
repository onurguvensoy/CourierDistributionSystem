package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "packages")
public class Package {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "courier_id")
    private User courier;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageStatus status = PackageStatus.PENDING;

    private LocalDateTime preferredDeliveryTime;
    
    private String deliveryTimeWindow;
    
    private String specialInstructions;

    public enum PackageStatus {
        PENDING,
        CREATED,
        ASSIGNED,
        PICKED_UP,
        DELIVERED,
        CANCELLED
    }

    // Alias methods for backward compatibility
    public PackageStatus getCurrentStatus() {
        return this.status;
    }

    public void setCurrentStatus(PackageStatus status) {
        this.status = status;
    }
} 