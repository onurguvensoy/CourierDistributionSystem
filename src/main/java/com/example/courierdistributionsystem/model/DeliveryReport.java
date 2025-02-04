package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    private DeliveryPackage deliveryPackage;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;

    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "customer_confirmation")
    private boolean customerConfirmation;

    @Column(name = "delivery_rating")
    private Integer deliveryRating;

    @Column(name = "delivery_photo_url")
    private String deliveryPhotoUrl;

    @Column(name = "signature_url")
    private String signatureUrl;

    @Column(name = "distance_traveled")
    private Double distanceTraveled;

    @PrePersist
    protected void onCreate() {
        if (completionTime == null) {
            completionTime = LocalDateTime.now();
        }
    }
} 