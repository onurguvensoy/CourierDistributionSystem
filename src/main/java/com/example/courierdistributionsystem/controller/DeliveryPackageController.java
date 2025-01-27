package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.service.DeliveryPackageService;
import com.example.courierdistributionsystem.service.CourierPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryPackageController {

    @Autowired
    private DeliveryPackageService deliveryPackageService;

    @Autowired
    private CourierPackageService courierPackageService;

    @GetMapping
    public ResponseEntity<?> getAllDeliveryPackages() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DeliveryPackage> deliveryPackages = deliveryPackageService.getAllDeliveryPackages();
            response.put("status", "success");
            response.put("data", deliveryPackages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeliveryPackageById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            DeliveryPackage deliveryPackage = deliveryPackageService.getDeliveryPackageById(id);
            response.put("status", "success");
            response.put("data", deliveryPackage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createDeliveryPackage(@RequestBody Map<String, String> deliveryRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            DeliveryPackage deliveryPackage = deliveryPackageService.createDeliveryPackage(deliveryRequest);
            response.put("status", "success");
            response.put("data", deliveryPackage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDeliveryPackage(@PathVariable Long id, @RequestBody Map<String, String> deliveryRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            DeliveryPackage deliveryPackage = deliveryPackageService.updateDeliveryPackage(id, deliveryRequest);
            response.put("status", "success");
            response.put("data", deliveryPackage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelDeliveryPackage(@PathVariable Long id, @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            deliveryPackageService.cancelDeliveryPackage(id, username);
            response.put("status", "success");
            response.put("message", "Delivery cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/courier/{username}")
    public ResponseEntity<?> getCourierDeliveryPackages(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DeliveryPackage> deliveryPackages = courierPackageService.getCourierActiveDeliveryPackages(username);
            response.put("status", "success");
            response.put("data", deliveryPackages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDeliveryPackages() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DeliveryPackage> deliveryPackages = courierPackageService.getAvailableDeliveryPackages();
            response.put("status", "success");
            response.put("data", deliveryPackages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignDeliveryPackage(@PathVariable Long id, @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            DeliveryPackage deliveryPackage = courierPackageService.takeDeliveryPackage(id, username);
            response.put("status", "success");
            response.put("data", deliveryPackage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam DeliveryPackage.DeliveryStatus status) {
        Map<String, Object> response = new HashMap<>();
        try {
            DeliveryPackage deliveryPackage = courierPackageService.updateDeliveryStatus(id, username, status);
            response.put("status", "success");
            response.put("data", deliveryPackage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/location")
    public ResponseEntity<?> updateDeliveryLocation(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String location) {
        Map<String, Object> response = new HashMap<>();
        try {
            DeliveryPackage deliveryPackage = courierPackageService.updateDeliveryLocation(id, username, latitude, longitude, location);
            response.put("status", "success");
            response.put("data", deliveryPackage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}/track")
    public ResponseEntity<?> trackDeliveryPackage(@PathVariable Long id, @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> trackingInfo = deliveryPackageService.trackDeliveryPackage(id, username);
            response.put("status", "success");
            response.put("data", trackingInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
