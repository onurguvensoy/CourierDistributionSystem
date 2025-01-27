package com.example.courierdistributionsystem.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Customer customer;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Courier courier;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "customer")
    @JsonBackReference
    private List<DeliveryPackage> customerDeliveries;

    @OneToMany(mappedBy = "courier")
    @JsonBackReference
    private List<DeliveryPackage> courierDeliveries;

    public enum UserRole {
        CUSTOMER,
        COURIER,
        ADMIN
    }
}
