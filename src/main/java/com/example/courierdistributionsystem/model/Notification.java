package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "delivery_package_id")
    private DeliveryPackage deliveryPackage;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(columnDefinition = "TEXT")
    private String message;

    private boolean isRead = false;

    private LocalDateTime createdAt;

    private String actionUrl;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        STATUS_CHANGE,
        DELIVERY_ALERT,
        RATING_REQUEST,
        SYSTEM_MESSAGE
    }
} 