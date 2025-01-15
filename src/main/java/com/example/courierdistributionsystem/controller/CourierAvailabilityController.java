package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.CourierAvailability;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.CourierAvailabilityRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/courier-availability")
public class CourierAvailabilityController {

    @Autowired
    private CourierAvailabilityRepository availabilityRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<CourierAvailability> getAllAvailabilities() {
        return availabilityRepository.findAll();
    }

    @GetMapping("/available")
    public List<CourierAvailability> getAvailableCouriers() {
        return availabilityRepository.findByAvailableTrue();
    }

    @GetMapping("/available-with-capacity")
    public List<CourierAvailability> getAvailableCouriersWithCapacity() {
        return availabilityRepository.findAvailableCouriersWithCapacity();
    }

    @GetMapping("/zone/{zone}")
    public List<CourierAvailability> getCouriersByZone(@PathVariable String zone) {
        return availabilityRepository.findByCurrentZone(zone);
    }

    @GetMapping("/my-status")
    public ResponseEntity<CourierAvailability> getMyAvailability(@RequestParam String username) {
        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CourierAvailability availability = availabilityRepository.findByCourier(courier)
                .orElseGet(() -> {
                    CourierAvailability newAvailability = new CourierAvailability();
                    newAvailability.setCourier(courier);
                    return availabilityRepository.save(newAvailability);
                });
        
        return ResponseEntity.ok(availability);
    }

    @PutMapping("/my-status")
    public ResponseEntity<CourierAvailability> updateMyAvailability(
            @Valid @RequestBody CourierAvailability availabilityDetails,
            @RequestParam String username) {
        
        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CourierAvailability availability = availabilityRepository.findByCourier(courier)
                .orElseGet(() -> {
                    CourierAvailability newAvailability = new CourierAvailability();
                    newAvailability.setCourier(courier);
                    return newAvailability;
                });

        availability.setAvailable(availabilityDetails.isAvailable());
        availability.setCurrentZone(availabilityDetails.getCurrentZone());
        availability.setCurrentPackageCount(availabilityDetails.getCurrentPackageCount());
        availability.setMaxPackageCapacity(availabilityDetails.getMaxPackageCapacity());
        availability.setCurrentLatitude(availabilityDetails.getCurrentLatitude());
        availability.setCurrentLongitude(availabilityDetails.getCurrentLongitude());

        CourierAvailability updatedAvailability = availabilityRepository.save(availability);
        return ResponseEntity.ok(updatedAvailability);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAvailability(@PathVariable Long id) {
        CourierAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Availability record not found"));
        availabilityRepository.delete(availability);
        return ResponseEntity.ok().build();
    }
} 