package com.example.courierdistributionsystem.model.role;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("ADMIN")
@NoArgsConstructor
public class AdminRole extends Role {
    @Override
    public String getRoleName() {
        return "ADMIN";
    }
} 