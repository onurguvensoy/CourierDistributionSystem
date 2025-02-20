package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.redis.core.RedisHash;

@Entity
@Table(name = "admins")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@OnDelete(action = OnDeleteAction.CASCADE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@RedisHash("admins")
public class Admin extends User {
    
    @Column(nullable = true)
    @Override
    public String getPhoneNumber() {
        return null;
    }

    @Override
    public void setPhoneNumber(String phoneNumber) {
    }

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (getRole() == null) {
            setRole(UserRole.ADMIN);
        }
    }
}