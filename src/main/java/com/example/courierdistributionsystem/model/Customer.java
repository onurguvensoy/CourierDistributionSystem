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
@EqualsAndHashCode(callSuper = true)
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id",
    scope = Customer.class)
@RedisHash("customers")
public class Customer extends User {
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference(value = "customer-packages")
    @ToString.Exclude
    @Builder.Default
    @JsonIgnoreProperties({"customer", "courier"})
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
        if (packages == null) {
            packages = new ArrayList<>();
        }
        return new ArrayList<>(packages);
    }

    @JsonProperty
    public void setPackages(List<DeliveryPackage> newPackages) {
        if (this.packages == null) {
            this.packages = new ArrayList<>();
        }
        this.packages.clear();
        if (newPackages != null) {
            for (DeliveryPackage pkg : newPackages) {
                pkg.setCustomer(this);
                this.packages.add(pkg);
            }
        }
    }
}