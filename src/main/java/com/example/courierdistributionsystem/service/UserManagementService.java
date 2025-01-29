package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.Admin;
import com.example.courierdistributionsystem.repository.CustomerRepository;
import com.example.courierdistributionsystem.repository.CourierRepository;
import com.example.courierdistributionsystem.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class UserManagementService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private AdminRepository adminRepository;

    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(customerRepository.findAll());
        allUsers.addAll(courierRepository.findAll());
        allUsers.addAll(adminRepository.findAll());
        return allUsers;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Courier> getAllCouriers() {
        return courierRepository.findAll();
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", getAllUsers().size());
        stats.put("totalCustomers", customerRepository.count());
        stats.put("totalCouriers", courierRepository.count());
        stats.put("totalAdmins", adminRepository.count());
        return stats;
    }

    @Transactional
    public void deleteUser(Long id, String role) {
        switch (User.UserRole.valueOf(role.toUpperCase())) {
            case CUSTOMER:
                customerRepository.deleteById(id);
                break;
            case COURIER:
                courierRepository.deleteById(id);
                break;
            case ADMIN:
                adminRepository.deleteById(id);
                break;
            default:
                throw new IllegalArgumentException("Invalid role type");
        }
    }

    public User getUserById(Long id, String role) {
        return switch (User.UserRole.valueOf(role.toUpperCase())) {
            case CUSTOMER -> customerRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            case COURIER -> courierRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Courier not found"));
            case ADMIN -> adminRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        };
    }
} 