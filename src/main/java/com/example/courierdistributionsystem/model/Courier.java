package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "couriers")
public class Courier extends User {

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean available = true;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "current_zone")
    private String currentZone;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @OneToMany(mappedBy = "courier")
    private List<DeliveryPackage> deliveries;

    @OneToMany(mappedBy = "courier")
    private List<DeliveryReport> reports;

    @OneToMany(mappedBy = "courier")
    private List<Rating> ratings;

    public void setIsAvailable(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return this.available;
    }

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (getRole() == null) {
            setRole(UserRole.COURIER);
        }
        if (averageRating == null) {
            averageRating = 0.0;
        }
    }
} 