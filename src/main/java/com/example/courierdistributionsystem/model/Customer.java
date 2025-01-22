package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer")
public class Customer {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "deliveryAddress")
    private String deliveryAddress;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "average_rating", nullable = false)
    private Double averageRating = 0.0;
} 