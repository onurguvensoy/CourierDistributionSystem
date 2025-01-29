package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("CUSTOMER")
@Table(name = "customers")
public class Customer extends User {
    private String phoneNumber;
    private String deliveryAddress;
    
    @Builder.Default
    private Double averageRating = 0.0;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (averageRating == null) {
            averageRating = 0.0;
        }
        setRole(UserRole.CUSTOMER);
    }
} 