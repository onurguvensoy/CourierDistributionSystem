/*
package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.PackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourierPackageService {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private UserRepository userRepository;


    public Package takePackage(Long packageId, String username) {
        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalStateException("Only couriers can accept packages");
        }

        Package pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        if (pkg.getStatus() != Package.PackageStatus.PENDING) {
            throw new IllegalStateException("Package is no longer available");
        }

        pkg.setCourier(courier);
        pkg.setStatus(Package.PackageStatus.ASSIGNED);
        pkg = packageRepository.save(pkg);

        
        return pkg;
    }

    public Package updateDeliveryStatus(Long packageId, String status, String username) {
        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalStateException("Only couriers can update package status");
        }

        Package pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        if (!pkg.getCourier().getId().equals(courier.getId())) {
            throw new IllegalStateException("You are not assigned to this package");
        }

        try {
            Package.PackageStatus newStatus = Package.PackageStatus.valueOf(status);
            pkg.setStatus(newStatus);
            pkg = packageRepository.save(pkg);
            

            
            return pkg;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid status");
        }
    }
} */
