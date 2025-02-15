package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.exception.CourierException;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.service.CourierService;
import com.example.courierdistributionsystem.service.UserService;
import com.example.courierdistributionsystem.service.DeliveryPackageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/courier")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated
public class CourierController {
    private static final Logger logger = LoggerFactory.getLogger(CourierController.class);

    @Autowired
    private DeliveryPackageService deliveryPackageService;

    @Autowired
    private CourierService courierService;

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourierProfile(@PathVariable @NotNull Long id) {
        logger.info("Received request to get courier profile with ID: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            logger.debug("Fetching courier profile for ID: {}", id);
            
    
            Optional<User> user = userService.getUserById(id);
            if (user.isEmpty()) {
                logger.warn("No user found with ID: {}", id);
                throw new CourierException("Courier not found");
            }

            if (user.get().getRole() != User.UserRole.COURIER) {
                logger.warn("User with ID {} is not a courier. Role: {}", id, user.get().getRole());
                throw new CourierException("User is not a courier");
            }

            Optional<Courier> courier = userService.getCourierById(id);
            if (courier.isEmpty()) {
                logger.warn("Courier details not found for ID: {}", id);
                throw new CourierException("Courier details not found");
            }

            response.put("status", "success");
            response.put("data", courier.get());
            logger.info("Successfully retrieved courier profile for ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (CourierException e) {
            logger.warn("Failed to fetch courier profile: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error fetching courier profile: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to fetch courier profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }


    @GetMapping("/available")
    public ResponseEntity<?> getAvailableCouriers() {
        logger.info("Received request to get available couriers");
        Map<String, Object> response = new HashMap<>();
        try {
            List<Courier> couriers = courierService.getAvailableCouriers();
            logger.debug("Found {} available couriers", couriers.size());
            response.put("status", "success");
            response.put("data", couriers);
            logger.info("Successfully retrieved available couriers");
            return ResponseEntity.ok(response);
        } catch (CourierException e) {
            logger.warn("Failed to fetch available couriers: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Failed to fetch available couriers: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to fetch available couriers: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourierProfile(
            @PathVariable @NotNull Long id,
            @RequestBody @Valid Map<String, String> courierRequest) {
        logger.info("Received request to update courier profile with ID: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            logger.debug("Updating courier profile with data: {}", courierRequest);
            Courier courier = courierService.updateCourierProfile(id, courierRequest);
            response.put("status", "success");
            response.put("data", courier);
            logger.info("Successfully updated courier profile with ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (CourierException e) {
            logger.warn("Failed to update courier profile: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error updating courier profile: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to update courier profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateCourierLocation(
            @PathVariable @NotNull Long id,
            @RequestBody @Valid Map<String, String> locationRequest) {
        logger.info("Received request to update courier location with ID: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            logger.debug("Updating courier location with data: {}", locationRequest);
            courierService.updateCourierLocation(id, locationRequest);
            response.put("status", "success");
            logger.info("Successfully updated courier location for ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (CourierException e) {
            logger.warn("Failed to update courier location: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error updating courier location: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to update courier location: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/{id}/availability")
    public ResponseEntity<?> updateAvailability(@PathVariable @NotNull Long id, @RequestParam boolean available) {
        logger.info("Received request to update courier availability. ID: {}, Available: {}", id, available);
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, String> updateRequest = new HashMap<>();
            updateRequest.put("available", String.valueOf(available));
            
            Courier updatedCourier = courierService.updateCourierProfile(id, updateRequest);
            
            response.put("status", "success");
            response.put("message", "Availability updated successfully");
            response.put("data", updatedCourier);
            logger.info("Successfully updated courier availability for ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (CourierException e) {
            logger.warn("Failed to update courier availability: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error updating courier availability: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to update courier availability: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/available-packages")
    public ResponseEntity<?> getAvailablePackages(HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Fetching available packages for courier: {}", username);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized access attempt to available packages");
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            List<DeliveryPackage> availablePackages = deliveryPackageService.getAvailableDeliveryPackages();
            logger.info("Successfully retrieved {} available packages for courier: {}", 
                availablePackages.size(), username);
            return ResponseEntity.ok(availablePackages);
        } catch (Exception e) {
            logger.error("Error fetching available packages for courier {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/active-deliveries")
    public ResponseEntity<?> getActiveDeliveries(HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Fetching active deliveries for courier: {}", username);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized access attempt to active deliveries");
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            List<DeliveryPackage> activeDeliveries = deliveryPackageService.getCourierActiveDeliveryPackages(username);
            logger.info("Successfully retrieved {} active deliveries for courier: {}", 
                activeDeliveries.size(), username);
            return ResponseEntity.ok(activeDeliveries);
        } catch (Exception e) {
            logger.error("Error fetching active deliveries for courier {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/packages/{packageId}/take")
    public ResponseEntity<?> takeDeliveryPackage(@PathVariable Long packageId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Courier {} attempting to take package {}", username, packageId);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized attempt to take package {}", packageId);
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            DeliveryPackage deliveryPackage = deliveryPackageService.takeDeliveryPackage(packageId, username);
            logger.info("Courier {} successfully took package {}", username, packageId);
            return ResponseEntity.ok(deliveryPackage);
        } catch (IllegalStateException e) {
            logger.warn("Business rule violation for courier {} taking package {}: {}", 
                username, packageId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing take package request for courier {} and package {}: {}", 
                username, packageId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/packages/{packageId}/drop")
    public ResponseEntity<?> dropDeliveryPackage(@PathVariable Long packageId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Courier {} attempting to drop package {}", username, packageId);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized attempt to drop package {}", packageId);
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            DeliveryPackage deliveryPackage = deliveryPackageService.dropDeliveryPackage(packageId, username);
            logger.info("Courier {} successfully dropped package {}", username, packageId);
            return ResponseEntity.ok(deliveryPackage);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for courier {} dropping package {}: {}", 
                username, packageId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing drop package request for courier {} and package {}: {}", 
                username, packageId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/packages/{packageId}/status")
    public ResponseEntity<?> updateDeliveryStatus(
            @PathVariable Long packageId,
            @RequestParam DeliveryPackage.DeliveryStatus status,
            HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Courier {} attempting to update package {} status to {}", username, packageId, status);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized attempt to update package {} status", packageId);
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            DeliveryPackage updatedPackage = deliveryPackageService.updateDeliveryStatus(packageId, username, status);
            logger.info("Courier {} successfully updated package {} status to {}", username, packageId, status);
            return ResponseEntity.ok(updatedPackage);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid status update request for courier {} and package {}: {}", 
                username, packageId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating status for courier {} and package {}: {}", 
                username, packageId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/packages/{packageId}/location")
    public ResponseEntity<?> updateDeliveryLocation(
            @PathVariable Long packageId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String location,
            HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Courier {} updating location for package {} to [{}, {}] - {}", 
            username, packageId, latitude, longitude, location);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized attempt to update package {} location", packageId);
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            DeliveryPackage updatedPackage = deliveryPackageService
                .updateDeliveryLocation(packageId, username, latitude, longitude, location);
            logger.info("Courier {} successfully updated package {} location", username, packageId);
            return ResponseEntity.ok(updatedPackage);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid location update request for courier {} and package {}: {}", 
                username, packageId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating location for courier {} and package {}: {}", 
                username, packageId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
}
