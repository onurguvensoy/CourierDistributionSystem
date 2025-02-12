package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.data.annotation.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "delivery_reports", timeToLive = 86400) 
public class DeliveryReport implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Indexed
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

    @Column(name = "delivery_photo_url")
    private String deliveryPhotoUrl;

    @Column(name = "signature_url")
    private String signatureUrl;

    @Column(name = "distance_traveled")
    private Double distanceTraveled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String reportType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String status;

    @PrePersist
    protected void onCreate() {
        if (completionTime == null) {
            completionTime = LocalDateTime.now();
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
} 