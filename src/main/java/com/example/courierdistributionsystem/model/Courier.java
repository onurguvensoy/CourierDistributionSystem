package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("COURIER")
@Table(name = "couriers")
public class Courier extends User {
    private String phoneNumber;
    private String vehicleType;
    
    @Column(name = "is_available")
    @Builder.Default
    private boolean available = true;
    
    private String currentZone;
    private Double currentLatitude;
    private Double currentLongitude;
    
    @Builder.Default
    private Integer maxPackageCapacity = 5;
    
    @Builder.Default
    private Integer currentPackageCount = 0;
    
    @Builder.Default
    private Double averageRating = 0.0;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (maxPackageCapacity == null) maxPackageCapacity = 5;
        if (currentPackageCount == null) currentPackageCount = 0;
        if (averageRating == null) averageRating = 0.0;
        setRole(UserRole.COURIER);
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
} 