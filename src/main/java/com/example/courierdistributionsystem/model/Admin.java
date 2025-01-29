package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("ADMIN")
@Table(name = "admin")
public class Admin extends User {
    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        setRole(UserRole.ADMIN);
    }
}