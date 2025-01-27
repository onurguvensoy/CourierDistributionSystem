package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.PackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PackageService {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketService webSocketService;

    public List<Package> getAllPackages() {
        return packageRepository.findAll();
    }

    public Package getPackageById(Long id) {
        return packageRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Package not found with id: " + id));
    }

    public Package createPackage(Map<String, String> packageRequest) {
        String username = packageRequest.get("username");
        String pickupAddress = packageRequest.get("pickupAddress");
        String deliveryAddress = packageRequest.get("deliveryAddress");
        String description = packageRequest.get("description");
        String weightStr = packageRequest.get("weight");

        if (username == null || pickupAddress == null || deliveryAddress == null) {
            throw new IllegalArgumentException("Missing required fields");
        }

        User customer = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (customer.getRole() != User.UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Only customers can create packages");
        }

        Package newPackage = Package.builder()
            .customer(customer)
            .pickupAddress(pickupAddress)
            .deliveryAddress(deliveryAddress)
            .description(description)
            .weight(weightStr != null ? Double.parseDouble(weightStr) : 0.0)
            .status(Package.PackageStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        Package savedPackage = packageRepository.save(newPackage);
        webSocketService.notifyNewPackageAvailable(savedPackage);
        return savedPackage;
    }

    public Package updatePackage(Long id, Map<String, String> packageRequest) {
        Package existingPackage = getPackageById(id);

        if (packageRequest.containsKey("status")) {
            existingPackage.setStatus(Package.PackageStatus.valueOf(packageRequest.get("status")));
        }
        if (packageRequest.containsKey("deliveryAddress")) {
            existingPackage.setDeliveryAddress(packageRequest.get("deliveryAddress"));
        }
        if (packageRequest.containsKey("pickupAddress")) {
            existingPackage.setPickupAddress(packageRequest.get("pickupAddress"));
        }
        if (packageRequest.containsKey("description")) {
            existingPackage.setDescription(packageRequest.get("description"));
        }
        if (packageRequest.containsKey("weight")) {
            existingPackage.setWeight(Double.parseDouble(packageRequest.get("weight")));
        }

        Package updatedPackage = packageRepository.save(existingPackage);
        if (updatedPackage.getCourier() != null) {
            webSocketService.notifyPackageStatusUpdate(updatedPackage);
        }
        return updatedPackage;
    }

    public void deletePackage(Long id) {
        Package package_ = getPackageById(id);
        if (package_.getStatus() != Package.PackageStatus.PENDING) {
            throw new IllegalArgumentException("Cannot delete package that is not in PENDING status");
        }
        packageRepository.delete(package_);
    }

    public List<Package> getCustomerPackages(String username) {
        User customer = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        if (customer.getRole() != User.UserRole.CUSTOMER) {
            throw new IllegalArgumentException("User is not a customer");
        }
        
        return packageRepository.findByCustomer(customer);
    }

    public List<Package> getCourierPackages(String username) {
        User courier = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));
        
        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalArgumentException("User is not a courier");
        }
        
        return packageRepository.findByCourier(courier);
    }

    public Package assignPackage(Long id, String username) {
        Package package_ = getPackageById(id);
        User courier = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));

        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalArgumentException("User is not a courier");
        }

        if (package_.getStatus() != Package.PackageStatus.PENDING) {
            throw new IllegalArgumentException("Package is not available for assignment");
        }

        package_.setCourier(courier);
        package_.setStatus(Package.PackageStatus.ASSIGNED);
        package_.setAssignedAt(LocalDateTime.now());

        Package updatedPackage = packageRepository.save(package_);
        webSocketService.notifyPackageStatusUpdate(updatedPackage);
        return updatedPackage;
    }
}
