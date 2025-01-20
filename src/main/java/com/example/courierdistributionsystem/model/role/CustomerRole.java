package com.example.courierdistributionsystem.model.role;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("CUSTOMER")
@Getter
@Setter
@NoArgsConstructor
public class CustomerRole extends Role {
    private String deliveryAddress;
    private String billingAddress;
    private String phoneNumber;

    @Override
    public String getRoleName() {
        return "CUSTOMER";
    }
}


