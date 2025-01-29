package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    public Customer updateCustomerProfile(Long id, Map<String, String> customerRequest) {
        Customer customer = getCustomerById(id);

        if (customerRequest.containsKey("phoneNumber")) {
            customer.setPhoneNumber(customerRequest.get("phoneNumber"));
        }
        if (customerRequest.containsKey("deliveryAddress")) {
            customer.setDeliveryAddress(customerRequest.get("deliveryAddress"));
        }

        return customerRepository.save(customer);
    }

    public Customer createCustomerProfile(Map<String, String> customerRequest) {
        String username = customerRequest.get("username");
        String email = customerRequest.get("email");
        String password = customerRequest.get("password");
        String phoneNumber = customerRequest.get("phoneNumber");
        String deliveryAddress = customerRequest.get("deliveryAddress");

        if (phoneNumber == null || deliveryAddress == null) {
            throw new IllegalArgumentException("Phone number and delivery address are required");
        }

        if (customerRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (customerRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        Customer customer = Customer.builder()
            .username(username)
            .email(email)
            .password(password)
            .role(User.UserRole.CUSTOMER)
            .phoneNumber(phoneNumber)
            .deliveryAddress(deliveryAddress)
            .averageRating(0.0)
            .build();

        return customerRepository.save(customer);
    }
} 