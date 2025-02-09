package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Table(name = "couriers")
@PrimaryKeyJoinColumn(name = "user_id")
@OnDelete(action = OnDeleteAction.CASCADE)
public class Courier extends User {

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    @Builder.Default
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
    @Builder.Default
    private Double averageRating = 1.0;

    @JsonIgnore
    @OneToMany(mappedBy = "courier", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<DeliveryPackage> deliveries;

    @JsonIgnore
    @OneToMany(mappedBy = "courier", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<DeliveryReport> reports;

    @JsonIgnore
    @OneToMany(mappedBy = "courier", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
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