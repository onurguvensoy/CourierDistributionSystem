package com.example.courierdistributionsystem.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.redis.core.RedisHash;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "packages"})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id",
    scope = Customer.class)
@RedisHash("customers")
public class Customer extends User {
    
    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "default_address")
    private String defaultAddress;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DeliveryPackage> packages;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (getRole() == null) {
            setRole(UserRole.CUSTOMER);
        }
    }

    @JsonIgnore
    public List<DeliveryPackage> getPackages() {
        if (packages == null) {
            packages = new ArrayList<>();
        }
        return packages;
    }

    public void setPackages(List<DeliveryPackage> newPackages) {
        this.packages = newPackages;
    }
}