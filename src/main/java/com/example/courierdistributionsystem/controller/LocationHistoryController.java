package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.LocationHistory;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.LocationHistoryRepository;
import com.example.courierdistributionsystem.repository.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/location-history")
public class LocationHistoryController {

    @Autowired
    private LocationHistoryRepository locationHistoryRepository;

    @Autowired
    private DeliveryPackageRepository packageRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<LocationHistory> getAllLocationHistory() {
        return locationHistoryRepository.findAll();
    }

    @GetMapping("/courier")
    public List<LocationHistory> getCourierLocationHistory(@RequestParam String username) {
        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return locationHistoryRepository.findByCourier(courier);
    }

    @GetMapping("/package/{packageId}")
    public List<LocationHistory> getPackageLocationHistory(@PathVariable Long packageId) {
        DeliveryPackage deliveryPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        return locationHistoryRepository.findByDeliveryPackage(deliveryPackage);
    }
}
