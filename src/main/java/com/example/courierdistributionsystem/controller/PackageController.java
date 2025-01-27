package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/package")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @GetMapping
    public ResponseEntity<?> getAllPackages() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Package> packages = packageService.getAllPackages();
            response.put("status", "success");
            response.put("data", packages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch packages: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createPackage(@RequestBody Map<String, String> packageRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            Package newPackage = packageService.createPackage(packageRequest);
            response.put("status", "success");
            response.put("data", newPackage);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create package: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPackageById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Package package_ = packageService.getPackageById(id);
            response.put("status", "success");
            response.put("data", package_);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch package: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePackage(@PathVariable Long id, @RequestBody Map<String, String> packageRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            Package updatedPackage = packageService.updatePackage(id, packageRequest);
            response.put("status", "success");
            response.put("data", updatedPackage);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update package: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePackage(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            packageService.deletePackage(id);
            response.put("status", "success");
            response.put("message", "Package deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete package: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/customer/{username}")
    public ResponseEntity<?> getCustomerPackages(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Package> packages = packageService.getCustomerPackages(username);
            response.put("status", "success");
            response.put("data", packages);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch customer packages: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/courier/{username}")
    public ResponseEntity<?> getCourierPackages(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Package> packages = packageService.getCourierPackages(username);
            response.put("status", "success");
            response.put("data", packages);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch courier packages: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<?> assignPackage(@PathVariable Long id, @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            Package assignedPackage = packageService.assignPackage(id, username);
            response.put("status", "success");
            response.put("data", assignedPackage);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to assign package: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
