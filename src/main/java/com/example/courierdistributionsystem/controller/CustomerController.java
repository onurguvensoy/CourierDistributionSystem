package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCustomerProfile(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByUserId(userId);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Customer profile not found");
                return ResponseEntity.badRequest().body(response);
            }
            response.put("status", "success");
            response.put("data", customer);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch customer profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> createCustomerProfile(@PathVariable Long userId, @RequestBody Map<String, String> customerRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.createCustomerProfile(userId, customerRequest);
            response.put("status", "success");
            response.put("data", customer);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create customer profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateCustomerProfile(@PathVariable Long userId, @RequestBody Map<String, String> customerRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.updateCustomerProfile(userId, customerRequest);
            response.put("status", "success");
            response.put("data", customer);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update customer profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 