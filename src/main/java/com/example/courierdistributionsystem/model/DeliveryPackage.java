package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.data.annotation.Id;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delivery_packages")
@RedisHash("delivery_packages")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "package_id")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryPackage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Indexed
    private Long package_id;

    @Column(unique = true, nullable = false)
    @Indexed
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference(value = "customer-packages")
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "courier_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("courierId")
    @ToString.Exclude
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

    @Column
    private Double latitude;

    @Column
    private Double longitude;

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



    @OneToMany(mappedBy = "deliveryPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    private List<DeliveryStatusHistory> statusHistory = new ArrayList<>();

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String formattedDeliveryDate;

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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getCustomerUsername() {
        return customer != null ? customer.getUsername() : null;
    }

    @JsonIgnore
    public void setCustomerUsername(String username) {
        // This is just a helper method to avoid deserialization errors
    }

    @JsonProperty("courierUsername")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getCourierUsername() {
        return courier != null ? courier.getUsername() : "N/A";
    }

    @JsonIgnore
    public void setCourierUsername(String username) {
        // This is just a helper method to avoid deserialization errors
    }

    private String generateTrackingNumber() {
        return String.format("CDS-%d-%04d", 
            System.currentTimeMillis() % 1000000000, 
            (int)(Math.random() * 10000));
    }

    @JsonProperty("id")
    public Long getId() {
        return package_id;
    }

    @JsonProperty("id")
    public void setId(Long id) {
        this.package_id = id;
    }

    @Override
    public String toString() {
        return "DeliveryPackage{" +
                "package_id=" + package_id +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", customerId=" + (customer != null ? customer.getId() : null) +
                ", courierId=" + (courier != null ? courier.getId() : null) +
                ", pickupAddress='" + pickupAddress + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", weight=" + weight +
                ", status=" + status +
                '}';
    }
}

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delivery_status_history")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
class DeliveryStatusHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "delivery_package_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private DeliveryPackage deliveryPackage;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    @JsonIdentityReference(alwaysAsId = true)
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