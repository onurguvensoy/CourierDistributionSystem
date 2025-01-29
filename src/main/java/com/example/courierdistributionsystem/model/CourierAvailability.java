package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "courier_id")
    private Courier courier;

    private boolean available;
    private String currentZone;
    private Integer currentPackageCount;
    private Integer maxPackageCapacity;
    private Double currentLatitude;
    private Double currentLongitude;
} 