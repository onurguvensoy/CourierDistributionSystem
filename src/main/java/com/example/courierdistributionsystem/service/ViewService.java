package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.Admin;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.repository.CustomerRepository;
import com.example.courierdistributionsystem.repository.CourierRepository;
import com.example.courierdistributionsystem.repository.AdminRepository;
import com.example.courierdistributionsystem.repository.DeliveryPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ViewService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DeliveryPackageRepository packageRepository;

    @Autowired
    private DeliveryPackageService deliveryPackageService;

    public User getUserByUsername(String username) {
        Customer customer = customerRepository.findByUsername(username).orElse(null);
        if (customer != null) {
            return customer;
        }
        
        Courier courier = courierRepository.findByUsername(username).orElse(null);
        if (courier != null) {
            return courier;
        }
        
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return admin;
    }

    public String getDashboardRedirect(String username) {
        User user = getUserByUsername(username);
        return switch (user.getRole()) {
            case CUSTOMER -> "redirect:/customer/dashboard";
            case COURIER -> "redirect:/courier/dashboard";
            case ADMIN -> "redirect:/admin/dashboard";
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
            (Courier) courier, 
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
        
        return packageRepository.findByCustomer((Customer) customer);
    }

    public void takeDeliveryPackage(Long packageId, String username) {
        deliveryPackageService.takeDeliveryPackage(packageId, username);
    }

    public void updateDeliveryStatus(Long packageId, String username, String status) {
        DeliveryPackage.DeliveryStatus newStatus = DeliveryPackage.DeliveryStatus.valueOf(status);
        deliveryPackageService.updateDeliveryStatus(packageId, username, newStatus);
    }

    public void dropDeliveryPackage(Long packageId, String username) {
        deliveryPackageService.dropDeliveryPackage(packageId, username);
    }
}