package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.CreatePackageDto;
import com.example.courierdistributionsystem.dto.CustomerDto;
import com.example.courierdistributionsystem.dto.DeliveryPackageDto;
import com.example.courierdistributionsystem.model.Customer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICustomerService {
    Customer createCustomer(CustomerDto customerDto);
    Customer updateCustomer(Long id, CustomerDto customerDto);
    Optional<Customer> getCustomerById(Long id);
    List<CustomerDto> getAllCustomers();
    void deleteCustomer(Long id);
    Optional<Customer> getCustomerByUsername(String username);
    Customer updateCustomerProfile(String username, Map<String, String> updates);
    Map<String, Object> getCustomerDashboard(String username);
    List<DeliveryPackageDto> getCustomerDeliveryHistory(String username);
    Customer createCustomerProfile(Map<String, String> profileData);
    DeliveryPackageDto createDeliveryPackage(String username, CreatePackageDto packageDto);
} 