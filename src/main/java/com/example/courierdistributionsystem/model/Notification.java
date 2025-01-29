package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    private Courier courier;

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

    public void setUser(Customer customer) {
        this.customer = customer;
        this.courier = null;
    }

    public void setUser(Courier courier) {
        this.courier = courier;
        this.customer = null;
    }

    public Object getUser() {
        return customer != null ? customer : courier;
    }

    public boolean belongsToUser(Customer customer) {
        return this.customer != null && this.customer.equals(customer);
    }

    public boolean belongsToUser(Courier courier) {
        return this.courier != null && this.courier.equals(courier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 