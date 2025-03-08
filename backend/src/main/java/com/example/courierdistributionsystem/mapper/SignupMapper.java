package com.example.courierdistributionsystem.mapper;

import com.example.courierdistributionsystem.dto.SignupDto;
import com.example.courierdistributionsystem.model.Admin;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.User;
import org.springframework.stereotype.Component;

@Component
public class SignupMapper {
    
  

    public SignupMapper() {
  
    }

    public User toEntity(SignupDto dto) {
        if (dto == null) {
            return null;
        }

        User.UserRole role = User.UserRole.valueOf(dto.getRole().toUpperCase());
        
        switch (role) {
            case CUSTOMER:
                return createCustomer(dto);
            case COURIER:
                return createCourier(dto);
            case ADMIN:
                return createAdmin(dto);
            default:
                throw new IllegalArgumentException("Invalid role: " + dto.getRole());
        }
    }

    private Customer createCustomer(SignupDto dto) {
        Customer customer = new Customer();
        setCommonFields(customer, dto);
        customer.setPhoneNumber(dto.getPhoneNumber());
        return customer;
    }

    private Courier createCourier(SignupDto dto) {
        Courier courier = new Courier();
        setCommonFields(courier, dto);
        courier.setPhoneNumber(dto.getPhoneNumber());
        courier.setVehicleType(dto.getVehicleType());
        courier.setAvailable(true);
        return courier;
    }

    private Admin createAdmin(SignupDto dto) {
        Admin admin = new Admin();
        setCommonFields(admin, dto);
        return admin;
    }

    private void setCommonFields(User user, SignupDto dto) {
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(User.UserRole.valueOf(dto.getRole().toUpperCase()));
    }
} 