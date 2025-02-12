package com.example.courierdistributionsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.redis.core.RedisHash;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@OnDelete(action = OnDeleteAction.CASCADE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@RedisHash("admins")
public class Admin extends User {
    
    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Builder.Default
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonManagedReference(value = "admin-reports")
    private List<DeliveryReport> reports = new ArrayList<>();

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