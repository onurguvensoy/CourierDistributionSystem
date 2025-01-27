package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.CustomerRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    public Customer getCustomerByUserId(Long userId) {
        return customerRepository.findByUserId(userId);
    }

    public Customer updateCustomerProfile(Long userId, Map<String, String> customerRequest) {
        Customer customer = getCustomerByUserId(userId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        if (customerRequest.containsKey("phoneNumber")) {
            customer.setPhoneNumber(customerRequest.get("phoneNumber"));
        }
        if (customerRequest.containsKey("deliveryAddress")) {
            customer.setDeliveryAddress(customerRequest.get("deliveryAddress"));
        }

        return customerRepository.save(customer);
    }

    public Customer createCustomerProfile(Long userId, Map<String, String> customerRequest) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != User.UserRole.CUSTOMER) {
            throw new IllegalArgumentException("User is not a customer");
        }

        if (customerRepository.findByUserId(userId) != null) {
            throw new IllegalArgumentException("Customer profile already exists");
        }

        String phoneNumber = customerRequest.get("phoneNumber");
        String deliveryAddress = customerRequest.get("deliveryAddress");

        if (phoneNumber == null || deliveryAddress == null) {
            throw new IllegalArgumentException("Phone number and delivery address are required");
        }

        Customer customer = Customer.builder()
            .user(user)
            .phoneNumber(phoneNumber)
            .deliveryAddress(deliveryAddress)
            .build();

        return customerRepository.save(customer);
    }
} 