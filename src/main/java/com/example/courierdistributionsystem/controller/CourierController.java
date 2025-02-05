package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.exception.CourierException;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.service.CourierService;
import com.example.courierdistributionsystem.service.UserService;
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
    private CourierService courierService;

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourierProfile(@PathVariable @NotNull Long id) {
        logger.info("Received request to get courier profile with ID: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            logger.debug("Fetching courier profile for ID: {}", id);
            
            // First check if user exists and is a courier
            Optional<User> user = userService.getUserById(id);
            if (user.isEmpty()) {
                logger.warn("No user found with ID: {}", id);
                throw new CourierException("Courier not found");
            }

            if (user.get().getRole() != User.UserRole.COURIER) {
                logger.warn("User with ID {} is not a courier. Role: {}", id, user.get().getRole());
                throw new CourierException("User is not a courier");
            }

            // Then get the courier details
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

    @GetMapping
    public ResponseEntity<?> getCourierProfileByParam(@RequestParam(required = true) @NotNull Long id) {
        logger.info("Received request to get courier profile with query parameter ID: {}", id);
        return getCourierProfile(id);
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
}
