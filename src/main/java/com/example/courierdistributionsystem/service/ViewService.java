package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.PackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ViewService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PackageRepository packageRepository;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public String getDashboardRedirect(String username) {
        User user = getUserByUsername(username);

        return switch (user.getRole()) {
            case ADMIN -> "redirect:/admin/dashboard";
            case COURIER -> "redirect:/courier/dashboard";
            case CUSTOMER -> "redirect:/customer/dashboard";

        };
    }

    public boolean isValidRole(User user, String requiredRole) {
        try {
            User.UserRole required = User.UserRole.valueOf(requiredRole);
            return user.getRole() == required;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public List<Package> getAvailablePackages() {
        return packageRepository.findByStatus(Package.PackageStatus.PENDING);
    }

    public List<Package> getActiveDeliveries(User courier) {
        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalStateException("Only couriers can view active deliveries");
        }

        return packageRepository.findByCourierAndStatusIn(
            courier, 
            List.of(Package.PackageStatus.ASSIGNED, Package.PackageStatus.PICKED_UP)
        );
    }

    public List<Package> getCustomerPackages(User customer) {
        if (customer.getRole() != User.UserRole.CUSTOMER) {
            throw new IllegalStateException("Only customers can view their packages");
        }
        
        return packageRepository.findByCustomer(customer);
    }
}