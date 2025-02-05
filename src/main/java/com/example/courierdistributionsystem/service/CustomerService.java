package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.exception.CustomerException;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.CustomerRepository;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Service
@Validated
@OnDelete(action = OnDeleteAction.CASCADE)
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    public Customer getCustomerById(Long id) {
        logger.debug("Fetching customer with ID: {}", id);
        return customerRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("Customer not found with ID: {}", id);
                return new CustomerException("Customer not found with ID: " + id);
            });
    }

    @Transactional
    public Customer createCustomerProfile(Map<String, String> customerRequest) {
        logger.debug("Creating new customer profile with data: {}", customerRequest);
        validateCustomerRequest(customerRequest);

        try {
            String username = customerRequest.get("username");
            String email = customerRequest.get("email");
            String password = customerRequest.get("password");
            String phoneNumber = customerRequest.get("phoneNumber");
            String deliveryAddress = customerRequest.get("deliveryAddress");

            if (customerRepository.findByUsername(username).isPresent()) {
                logger.warn("Username already exists: {}", username);
                throw new CustomerException("Username already exists");
            }

            if (customerRepository.findByEmail(email).isPresent()) {
                logger.warn("Email already exists: {}", email);
                throw new CustomerException("Email already exists");
            }

            Customer customer = Customer.builder()
                .username(username)
                .email(email)
                .password(password)
                .role(User.UserRole.CUSTOMER)
                .phoneNumber(phoneNumber)
                .deliveryAddress(deliveryAddress)
                .build();

            Customer savedCustomer = customerRepository.save(customer);
            logger.info("Successfully created customer profile with ID: {}", savedCustomer.getId());
            return savedCustomer;
        } catch (CustomerException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create customer profile: {}", e.getMessage(), e);
            throw new CustomerException("Failed to create customer profile: " + e.getMessage());
        }
    }

    @Transactional
    public Customer updateCustomerProfile(Long id, Map<String, String> customerRequest) {
        logger.debug("Updating customer profile for ID: {} with data: {}", id, customerRequest);
        validateCustomerUpdateRequest(customerRequest);

        try {
            Customer customer = getCustomerById(id);
            updateCustomerFields(customer, customerRequest);
            Customer updatedCustomer = customerRepository.save(customer);
            logger.info("Successfully updated customer profile for ID: {}", id);
            return updatedCustomer;
        } catch (CustomerException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update customer profile: {}", e.getMessage(), e);
            throw new CustomerException("Failed to update customer profile: " + e.getMessage());
        }
    }

    private void validateCustomerRequest(Map<String, String> request) {
        logger.debug("Validating customer request: {}", request);
        
        if (request == null || request.isEmpty()) {
            logger.warn("Empty customer request received");
            throw new CustomerException("Customer request cannot be empty");
        }

        String[] requiredFields = {"username", "email", "password", "phoneNumber", "deliveryAddress"};
        for (String field : requiredFields) {
            if (!request.containsKey(field) || request.get(field) == null || request.get(field).trim().isEmpty()) {
                logger.warn("Missing required field: {}", field);
                throw new CustomerException("Missing required field: " + field);
            }
        }

        if (!isValidPhoneNumber(request.get("phoneNumber"))) {
            logger.warn("Invalid phone number format: {}", request.get("phoneNumber"));
            throw new CustomerException("Invalid phone number format");
        }

        if (!isValidEmail(request.get("email"))) {
            logger.warn("Invalid email format: {}", request.get("email"));
            throw new CustomerException("Invalid email format");
        }
    }

    private void validateCustomerUpdateRequest(Map<String, String> request) {
        logger.debug("Validating customer update request: {}", request);
        
        if (request == null || request.isEmpty()) {
            logger.warn("Empty update request received");
            throw new CustomerException("Update request cannot be empty");
        }

        if (request.containsKey("phoneNumber") && !isValidPhoneNumber(request.get("phoneNumber"))) {
            logger.warn("Invalid phone number format: {}", request.get("phoneNumber"));
            throw new CustomerException("Invalid phone number format");
        }

        if (request.containsKey("email") && !isValidEmail(request.get("email"))) {
            logger.warn("Invalid email format: {}", request.get("email"));
            throw new CustomerException("Invalid email format");
        }
    }

    private void updateCustomerFields(Customer customer, Map<String, String> request) {
        if (request.containsKey("phoneNumber")) {
            customer.setPhoneNumber(request.get("phoneNumber"));
        }
        if (request.containsKey("deliveryAddress")) {
            customer.setDeliveryAddress(request.get("deliveryAddress"));
        }
        if (request.containsKey("email")) {
            customer.setEmail(request.get("email"));
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
} 