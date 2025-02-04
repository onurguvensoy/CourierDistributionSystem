package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Admin;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.AdminRepository;
import com.example.courierdistributionsystem.repository.CustomerRepository;
import com.example.courierdistributionsystem.repository.CourierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
public class UserManagementService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Transactional(readOnly = true)
    public Optional<? extends User> findByUsername(String username) {
        Optional<Admin> admin = adminRepository.findByUsername(username);
        if (admin.isPresent()) {
            return admin;
        }

        Optional<Customer> customer = customerRepository.findByUsername(username);
        if (customer.isPresent()) {
            return customer;
        }

        return courierRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<? extends User> findByEmail(String email) {
        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            return admin;
        }

        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isPresent()) {
            return customer;
        }

        return courierRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username) ||
               customerRepository.existsByUsername(username) ||
               courierRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return adminRepository.existsByEmail(email) ||
               customerRepository.existsByEmail(email) ||
               courierRepository.existsByEmail(email);
    }

    @Transactional
    public Admin saveAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public Courier saveCourier(Courier courier) {
        return courierRepository.save(courier);
    }

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
    public void deleteUser(User user) {
        if (user instanceof Admin) {
            adminRepository.delete((Admin) user);
        } else if (user instanceof Customer) {
            customerRepository.delete((Customer) user);
        } else if (user instanceof Courier) {
            courierRepository.delete((Courier) user);
        }
    }
} 