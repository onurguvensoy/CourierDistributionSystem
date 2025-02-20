package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.data.annotation.Id;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import com.example.courierdistributionsystem.utils.JpaConverterJson;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_package_id", nullable = false)
    private DeliveryPackage deliveryPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private LocalDateTime deliveryTime;

    @Column(columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "customer_signature")
    private String customerSignature;

    @Column(name = "delivery_proof_photo")
    private String deliveryProofPhoto;

    @Convert(converter = JpaConverterJson.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> additionalDetails;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    @Column(name = "customer_confirmation")
    private boolean customerConfirmation;

    @Column(name = "delivery_photo_url")
    private String deliveryPhotoUrl;

    @Column(name = "signature_url")
    private String signatureUrl;

    @Column(name = "distance_traveled")
    private Double distanceTraveled;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void setCompletionTime(LocalDateTime completionTime) {
        this.completionTime = completionTime;
    }

    public boolean isCustomerConfirmation() {
        return customerConfirmation;
    }

    public String getDeliveryPhotoUrl() {
        return deliveryPhotoUrl;
    }

    public String getSignatureUrl() {
        return signatureUrl;
    }

    public Double getDistanceTraveled() {
        return distanceTraveled;
    }
} 