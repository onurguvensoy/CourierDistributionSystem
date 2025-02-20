package com.example.courierdistributionsystem.service.impl;

import com.example.courierdistributionsystem.dto.CreatePackageDto;
import com.example.courierdistributionsystem.dto.CustomerDto;
import com.example.courierdistributionsystem.dto.DeliveryPackageDto;
import com.example.courierdistributionsystem.exception.ResourceNotFoundException;
import com.example.courierdistributionsystem.mapper.CustomerMapper;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.repository.jpa.CustomerRepository;
import com.example.courierdistributionsystem.service.ICustomerService;
import com.example.courierdistributionsystem.service.IDeliveryPackageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CustomerServiceImpl implements ICustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final IDeliveryPackageService deliveryPackageService;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository,
                             CustomerMapper customerMapper,
                             IDeliveryPackageService deliveryPackageService) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.deliveryPackageService = deliveryPackageService;
    }

    @Override
    public Customer createCustomer(CustomerDto customerDto) {
        logger.debug("Creating new customer from DTO");
        Customer customer = customerMapper.toEntity(customerDto);
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, CustomerDto customerDto) {
        logger.debug("Updating customer with ID: {}", id);
        Customer existingCustomer = getCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        
        // Update customer fields from DTO
        existingCustomer.setEmail(customerDto.getEmail());
        existingCustomer.setPhoneNumber(customerDto.getPhoneNumber());
        existingCustomer.setDefaultAddress(customerDto.getDefaultAddress());
        
        return customerRepository.save(existingCustomer);
    }

    @Override
    public Optional<Customer> getCustomerById(Long id) {
        logger.debug("Fetching customer by ID: {}", id);
        return customerRepository.findById(id);
    }

    @Override
    public List<CustomerDto> getAllCustomers() {
        logger.debug("Fetching all customers");
        return customerRepository.findAll().stream()
                .map(customerMapper::toDto)
                .toList();
    }

    @Override
    public void deleteCustomer(Long id) {
        logger.debug("Deleting customer with ID: {}", id);
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Override
    public Optional<Customer> getCustomerByUsername(String username) {
        logger.debug("Fetching customer by username: {}", username);
        return customerRepository.findByUsername(username);
    }

    @Override
    public Customer updateCustomerProfile(String username, Map<String, String> updates) {
        logger.debug("Updating profile for customer: {}", username);
        Customer customer = getCustomerByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with username: " + username));

        if (updates.containsKey("email")) {
            customer.setEmail(updates.get("email"));
        }
        if (updates.containsKey("phoneNumber")) {
            customer.setPhoneNumber(updates.get("phoneNumber"));
        }
        if (updates.containsKey("defaultAddress")) {
            customer.setDefaultAddress(updates.get("defaultAddress"));
        }

        return customerRepository.save(customer);
    }

    @Override
    public Map<String, Object> getCustomerDashboard(String username) {
        logger.debug("Getting dashboard for customer: {}", username);
        Customer customer = getCustomerByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with username: " + username));

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("profile", customerMapper.toDto(customer));
        dashboard.put("activeDeliveries", deliveryPackageService.getCustomerActiveDeliveryPackages(username));
        dashboard.put("deliveryHistory", getCustomerDeliveryHistory(username));
        
        return dashboard;
    }

    @Override
    public List<DeliveryPackageDto> getCustomerDeliveryHistory(String username) {
        logger.debug("Getting delivery history for customer: {}", username);
        return deliveryPackageService.getCustomerDeliveryHistory(username);
    }

    @Override
    public Customer createCustomerProfile(Map<String, String> profileData) {
        logger.debug("Creating customer profile with data: {}", profileData);
        Customer customer = new Customer();
        customer.setUsername(profileData.get("username"));
        customer.setEmail(profileData.get("email"));
        customer.setPhoneNumber(profileData.get("phoneNumber"));
        customer.setDefaultAddress(profileData.get("defaultAddress"));
        customer.setPassword(profileData.get("password")); // Note: Should be encoded before saving
        
        return customerRepository.save(customer);
    }

    @Override
    public DeliveryPackageDto createDeliveryPackage(String username, CreatePackageDto packageDto) {
        logger.debug("Creating delivery package for customer: {}", username);
        Customer customer = getCustomerByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with username: " + username));
        
        return deliveryPackageService.createDeliveryPackage(packageDto);
    }
} 