/*
package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.PackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PackageService {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketService webSocketService;

    public Package createPackage(Package packageRequest, String username) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (customer.getRole() != User.UserRole.CUSTOMER) {
            throw new IllegalStateException("Only customers can create packages");
        }
        
        packageRequest.setCustomer(customer);
        packageRequest.setStatus(Package.PackageStatus.PENDING);
        
        Package savedPackage = packageRepository.save(packageRequest);
        webSocketService.notifyNewPackageAvailable(savedPackage);
        
        return savedPackage;
    }

    public Package getPackageById(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));
    }

    public Package takePackage(Long packageId, String username) {
        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalStateException("Only couriers can accept packages");
        }

        Package pkg = getPackageById(packageId);

        if (pkg.getStatus() != Package.PackageStatus.PENDING) {
            throw new IllegalStateException("Package is no longer available");
        }

        pkg.setCourier(courier);
        pkg.setStatus(Package.PackageStatus.ASSIGNED);
        pkg = packageRepository.save(pkg);
        
        webSocketService.notifyPackageStatusUpdate(pkg);
        
        return pkg;
    }

    public Package updateDeliveryStatus(Long packageId, String status, String username) {
        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalStateException("Only couriers can update package status");
        }

        Package pkg = getPackageById(packageId);

        if (!pkg.getCourier().getId().equals(courier.getId())) {
            throw new IllegalStateException("You are not assigned to this package");
        }

        try {
            Package.PackageStatus newStatus = Package.PackageStatus.valueOf(status);
            pkg.setStatus(newStatus);
            pkg = packageRepository.save(pkg);
            
            webSocketService.notifyPackageStatusUpdate(pkg);
            
            return pkg;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid status");
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
} */
