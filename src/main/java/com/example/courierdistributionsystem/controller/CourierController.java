package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courier")
public class CourierController {

    @Autowired
    private CourierService courierService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourierProfile(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Courier courier = courierService.getCourierById(id);
            response.put("status", "success");
            response.put("data", courier);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch courier profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableCouriers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Courier> couriers = courierService.getAvailableCouriers();
            response.put("status", "success");
            response.put("data", couriers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch available couriers: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createCourierProfile(@RequestBody Map<String, String> courierRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            Courier courier = courierService.createCourierProfile(courierRequest);
            response.put("status", "success");
            response.put("data", courier);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create courier profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourierProfile(@PathVariable Long id, @RequestBody Map<String, String> courierRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            Courier courier = courierService.updateCourierProfile(id, courierRequest);
            response.put("status", "success");
            response.put("data", courier);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update courier profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateCourierLocation(@PathVariable Long id, @RequestBody Map<String, String> locationRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            courierService.updateCourierLocation(id, locationRequest);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update courier location: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
