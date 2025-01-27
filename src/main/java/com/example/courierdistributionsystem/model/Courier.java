package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "couriers")
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String phoneNumber;
    private String vehicleType;
    
    @Column(name = "is_available")
    private boolean available;
    
    private String currentZone;
    private Double currentLatitude;
    private Double currentLongitude;
    private Integer maxPackageCapacity;
    private Integer currentPackageCount;
    private Double averageRating;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (maxPackageCapacity == null) maxPackageCapacity = 5;
        if (currentPackageCount == null) currentPackageCount = 0;
        if (averageRating == null) averageRating = 0.0;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
} 