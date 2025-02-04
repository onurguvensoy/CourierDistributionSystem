package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.dto.CreatePackageRequest;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.service.DeliveryPackageService;
import com.example.courierdistributionsystem.service.ViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    @Autowired
    private DeliveryPackageService packageService;

    @Autowired
    private ViewService viewService;

    @PostMapping("/create")
    public ResponseEntity<?> createPackage(@RequestBody CreatePackageRequest request,
                                         @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Customer customer = (Customer) viewService.getUserByUsername(username);
            DeliveryPackage newPackage = packageService.createPackage(request, customer);
            
            response.put("status", "success");
            response.put("message", "Package delivery request created successfully");
            response.put("data", newPackage);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 