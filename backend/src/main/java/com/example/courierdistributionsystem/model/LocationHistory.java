package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "location_history")
@RedisHash("location_history")
public class LocationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private DeliveryPackage deliveryPackage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    private String zone;
    
    @Column(name = "courier_username", nullable = false)
    private String courierUsername;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (courier != null && courierUsername == null) {
            courierUsername = courier.getUsername();
        }
    }
} 