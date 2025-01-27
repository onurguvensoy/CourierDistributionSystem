package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "delivery_package_id")
    private DeliveryPackage deliveryPackage;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    private User courier;

    private LocalDateTime deliveryTime;
    private String deliveryNotes;
    private boolean customerConfirmation;
    private Integer deliveryRating;
    private String deliveryPhotoUrl;
    private String signatureUrl;
    private Double distanceTraveled;
    private LocalDateTime completionTime;
} 