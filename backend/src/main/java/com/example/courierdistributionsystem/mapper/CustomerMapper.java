package com.example.courierdistributionsystem.mapper;

import com.example.courierdistributionsystem.dto.CustomerDto;
import com.example.courierdistributionsystem.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerDto toDto(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setUsername(customer.getUsername());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setDefaultAddress(customer.getDefaultAddress());
        dto.setRole(customer.getRole());
        return dto;
    }

    public Customer toEntity(CustomerDto dto) {
        if (dto == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setUsername(dto.getUsername());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setRole(dto.getRole());
        return customer;
    }
} 