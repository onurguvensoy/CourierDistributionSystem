package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "performance_metrics")
public class PerformanceMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;

    private LocalDate date;

    private String zone;

    private Integer totalDeliveries = 0;

    private Integer successfulDeliveries = 0;

    private Integer failedDeliveries = 0;

    @Column(columnDefinition = "DOUBLE")
    private Double averageDeliveryTime;

    @Column(columnDefinition = "DOUBLE")
    private Double totalDistance;

    @Column(columnDefinition = "DOUBLE")
    private Double totalRevenue;

    @Column(columnDefinition = "DOUBLE")
    private Double customerSatisfactionScore;

    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDate.now();
        }
    }
} 