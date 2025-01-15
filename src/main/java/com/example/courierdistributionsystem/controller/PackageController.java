package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.PackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Package> getAllPackages() {
        return packageRepository.findAll();
    }

    @GetMapping("/my-packages")
    public List<Package> getMyPackages(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return packageRepository.findByCustomer(user);
    }

    @GetMapping("/assigned-packages")
    public List<Package> getAssignedPackages(@RequestParam String username) {
        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return packageRepository.findByCourier(courier);
    }

    @PostMapping
    public Package createPackage(@Valid @RequestBody Package packageRequest, @RequestParam String username) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        packageRequest.setCustomer(customer);
        packageRequest.setCurrentStatus(Package.PackageStatus.CREATED);
        return packageRepository.save(packageRequest);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Package> getPackageById(@PathVariable Long id) {
        Package package_ = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));
        return ResponseEntity.ok(package_);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Package> updatePackage(@PathVariable Long id, @Valid @RequestBody Package packageDetails) {
        Package package_ = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));

        package_.setCurrentStatus(packageDetails.getCurrentStatus());
        package_.setDeliveryAddress(packageDetails.getDeliveryAddress());
        package_.setPickupAddress(packageDetails.getPickupAddress());
        package_.setDescription(packageDetails.getDescription());
        package_.setPreferredDeliveryTime(packageDetails.getPreferredDeliveryTime());
        package_.setDeliveryTimeWindow(packageDetails.getDeliveryTimeWindow());
        package_.setSpecialInstructions(packageDetails.getSpecialInstructions());

        Package updatedPackage = packageRepository.save(package_);
        return ResponseEntity.ok(updatedPackage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePackage(@PathVariable Long id) {
        Package package_ = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));
        packageRepository.delete(package_);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<Package> assignPackage(@PathVariable Long id, @RequestParam String username) {
        Package package_ = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));
        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        package_.setCourier(courier);
        package_.setCurrentStatus(Package.PackageStatus.ASSIGNED);
        Package updatedPackage = packageRepository.save(package_);
        return ResponseEntity.ok(updatedPackage);
    }
} 