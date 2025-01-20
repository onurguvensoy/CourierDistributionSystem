package com.example.courierdistributionsystem.model.role;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("COURIER")
@Getter
@Setter
@NoArgsConstructor
public class CourierRole extends Role {
    private Double averageRating;
    private Boolean isAvailable;
    private String currentLocation;


    @Override
    public String getRoleName() {
        return "COURIER";
    }
} 