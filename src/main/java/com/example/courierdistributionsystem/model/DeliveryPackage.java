package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delivery_packages")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class DeliveryPackage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String trackingNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("customerId")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("courierId")
    private Courier courier;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    private String description;

    @Column
    private String specialInstructions;

    @Column
    private Double currentLatitude;

    @Column
    private Double currentLongitude;

    @Column
    private String currentLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @OneToOne(mappedBy = "deliveryPackage", cascade = CascadeType.ALL)
    private Rating rating;

    @OneToMany(mappedBy = "deliveryPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryStatusHistory> statusHistory = new ArrayList<>();

    public enum DeliveryStatus {
        PENDING,
        ASSIGNED,
        PICKED_UP,
        IN_TRANSIT,
        DELIVERED,
        CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (status == null) {
            status = DeliveryStatus.PENDING;
        }
        if (trackingNumber == null) {
            trackingNumber = generateTrackingNumber();
        }
        updatedAt = now;
        addStatusHistory("Package created", null);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void setStatus(DeliveryStatus newStatus) {
        if (this.status != newStatus) {
            this.status = newStatus;
            this.updatedAt = LocalDateTime.now();
            
            switch (newStatus) {
                case PENDING -> {}
                case ASSIGNED -> this.assignedAt = LocalDateTime.now();
                case PICKED_UP -> this.pickedUpAt = LocalDateTime.now();
                case IN_TRANSIT -> this.updatedAt = LocalDateTime.now();
                case DELIVERED -> this.deliveredAt = LocalDateTime.now();
                case CANCELLED -> this.cancelledAt = LocalDateTime.now();
            }
            
            addStatusHistory("Status changed to " + newStatus, null);
        }
    }

    public void updateLocation(Double latitude, Double longitude, String location) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.currentLocation = location;
        this.updatedAt = LocalDateTime.now();
        
        addStatusHistory("Location updated to: " + location, 
            String.format("Lat: %f, Long: %f", latitude, longitude));
    }

    private void addStatusHistory(String notes, String locationData) {
        DeliveryStatusHistory history = DeliveryStatusHistory.builder()
            .deliveryPackage(this)
            .status(this.status)
            .courier(this.courier)
            .notes(notes)
            .locationData(locationData)
            .createdAt(LocalDateTime.now())
            .build();
        
        this.statusHistory.add(history);
    }

    @JsonProperty("customerUsername")
    public String getCustomerUsername() {
        return customer != null ? customer.getUsername() : null;
    }

    @JsonProperty("courierUsername")
    public String getCourierUsername() {
        return courier != null ? courier.getUsername() : null;
    }

    private String generateTrackingNumber() {
        // Generate a unique tracking number with format: CDS-TIMESTAMP-RANDOM
        return String.format("CDS-%d-%04d", 
            System.currentTimeMillis() % 1000000000, 
            (int)(Math.random() * 10000));
    }
}

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delivery_status_history")
class DeliveryStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long package_id;

    @ManyToOne
    @JoinColumn(name = "delivery_package_id", nullable = false)
    private DeliveryPackage deliveryPackage;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    private Courier courier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryPackage.DeliveryStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String locationData;

    @Column(nullable = false)
    private LocalDateTime createdAt;
} 