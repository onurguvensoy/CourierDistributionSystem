package com.example.courierdistributionsystem.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
@RedisHash("customers")
public class Customer extends User {
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference(value = "customer-packages")
    @ToString.Exclude
    @Builder.Default
    private List<DeliveryPackage> packages = new ArrayList<>();

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (getRole() == null) {
            setRole(UserRole.CUSTOMER);
        }
    }

    @JsonProperty
    public List<DeliveryPackage> getPackages() {
        return packages != null ? new ArrayList<>(packages) : new ArrayList<>();
    }

    public void setPackages(List<DeliveryPackage> packages) {
        if (this.packages != null) {
            this.packages.clear();
            if (packages != null) {
                this.packages.addAll(packages);
            }
        }
    }
}