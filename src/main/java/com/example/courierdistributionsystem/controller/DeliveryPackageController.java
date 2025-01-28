package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.service.DeliveryPackageService;
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

    @GetMapping
    public ResponseEntity<?> getAllDeliveryPackages() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DeliveryPackage> packages = deliveryPackageService.getAllDeliveryPackages();
            response.put("status", "success");
            response.put("data", packages);
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
            List<DeliveryPackage> packages = deliveryPackageService.getAvailableDeliveryPackages();
            response.put("status", "success");
            response.put("data", packages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getCourierActiveDeliveryPackages(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DeliveryPackage> packages = deliveryPackageService.getCourierActiveDeliveryPackages(username);
            response.put("status", "success");
            response.put("data", packages);
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
            DeliveryPackage newPackage = deliveryPackageService.createDeliveryPackage(deliveryRequest);
            response.put("status", "success");
            response.put("data", newPackage);
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
            DeliveryPackage assignedPackage = deliveryPackageService.takeDeliveryPackage(id, username);
            response.put("status", "success");
            response.put("data", assignedPackage);
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
            DeliveryPackage updatedPackage = deliveryPackageService.updateDeliveryStatus(id, username, status);
            response.put("status", "success");
            response.put("data", updatedPackage);
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
            DeliveryPackage updatedPackage = deliveryPackageService.updateDeliveryLocation(id, username, latitude, longitude, location);
            response.put("status", "success");
            response.put("data", updatedPackage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/drop")
    public ResponseEntity<?> dropDeliveryPackage(@PathVariable Long id, @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            DeliveryPackage droppedPackage = deliveryPackageService.dropDeliveryPackage(id, username);
            response.put("status", "success");
            response.put("data", droppedPackage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/customer")
    public ResponseEntity<?> getCustomerDeliveryPackages(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DeliveryPackage> packages = deliveryPackageService.getCustomerDeliveryPackages(username);
            response.put("status", "success");
            response.put("data", packages);
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
