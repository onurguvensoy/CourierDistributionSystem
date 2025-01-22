package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courier")
public class Courier {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "isAvailable")
    private boolean isAvailable = true;

} 