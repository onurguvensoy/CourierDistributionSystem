package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ViewService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeliveryPackageRepository packageRepository;

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

    public List<DeliveryPackage> getAvailablePackages() {
        return packageRepository.findByStatus(DeliveryPackage.DeliveryStatus.PENDING);
    }

    public List<DeliveryPackage> getActiveDeliveries(User courier) {
        if (courier == null) {
            throw new IllegalArgumentException("Courier cannot be null");
        }
        
        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalStateException("Only couriers can view active deliveries");
        }

        return packageRepository.findByCourierAndStatusIn(
            courier, 
            List.of(DeliveryPackage.DeliveryStatus.ASSIGNED, DeliveryPackage.DeliveryStatus.PICKED_UP, DeliveryPackage.DeliveryStatus.IN_TRANSIT)
        );
    }

    public List<DeliveryPackage> getCustomerPackages(User customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        
        if (customer.getRole() != User.UserRole.CUSTOMER) {
            throw new IllegalStateException("Only customers can view their packages");
        }
        
        return packageRepository.findByCustomer(customer);
    }
}