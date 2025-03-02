package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "delivery_histories")
public class DeliveryHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private Courier courier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private DeliveryPackage deliveryPackage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "delivery_status")
    @Enumerated(EnumType.STRING)
    private DeliveryPackage.DeliveryStatus status;

    @Column(name = "pickup_location")
    private String pickupLocation;

    @Column(name = "delivery_location")
    private String deliveryLocation;

    @Column(name = "delivery_notes")
    private String deliveryNotes;

    @Column(name = "customer_feedback")
    private String customerFeedback;

    @Column(name = "delivery_rating")
    private Integer deliveryRating;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 