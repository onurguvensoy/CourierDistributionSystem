package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.dto.CreatePackageRequest;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.service.CustomerService;
import com.example.courierdistributionsystem.service.DeliveryPackageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/packages")
public class DeliveryPackageController {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryPackageController.class);

    @Autowired
    private DeliveryPackageService deliveryPackageService;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPackage(@Valid @RequestBody CreatePackageRequest request, HttpSession session) {
        try {
            // Get username from session
            String username = (String) session.getAttribute("username");
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("User not authenticated");
            }

            Optional<Customer> customer = customerService.findByUsername(username);
            if (customer.isEmpty()) {
                throw new IllegalArgumentException("Customer not found");
            }

            DeliveryPackage newPackage = deliveryPackageService.createPackage(request, customer.get());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Package created successfully");
            response.put("package", newPackage);
            response.put("packageId", newPackage.getPackage_id());

            logger.info("Package created successfully with ID: {}", newPackage.getPackage_id());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to create package: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}/track")
    public ResponseEntity<?> trackPackage(@PathVariable Long id, @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("Tracking package {} for user {}", id, username);
            Map<String, Object> trackingInfo = deliveryPackageService.trackDeliveryPackage(id, username);
            response.put("status", "success");
            response.put("data", trackingInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to track package: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

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
    public ResponseEntity<?> createDeliveryPackage(
            @RequestBody Map<String, String> deliveryRequest,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get username from session
            String username = (String) session.getAttribute("username");
            if (username == null) {
                throw new IllegalArgumentException("User not authenticated");
            }

            // Add username to the request
            deliveryRequest.put("username", username);

            // Validate required fields
            String[] requiredFields = {"pickupAddress", "deliveryAddress", "weight", "description"};
            for (String field : requiredFields) {
                if (!deliveryRequest.containsKey(field) || deliveryRequest.get(field).trim().isEmpty()) {
                    throw new IllegalArgumentException("Missing required field: " + field);
                }
            }

            // Create package
            DeliveryPackage newPackage = deliveryPackageService.createDeliveryPackage(deliveryRequest);
            
            response.put("status", "success");
            response.put("message", "Delivery package created successfully");
            response.put("data", newPackage);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignPackage(@PathVariable Long id, @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("Assigning package {} to courier {}", id, username);
            DeliveryPackage assignedPackage = deliveryPackageService.takeDeliveryPackage(id, username);
            response.put("status", "success");
            response.put("message", "Package assigned successfully");
            response.put("data", assignedPackage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to assign package: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("Updating package {} status to {} by courier {}", id, status, username);
            DeliveryPackage.DeliveryStatus newStatus = DeliveryPackage.DeliveryStatus.valueOf(status.toUpperCase());
            DeliveryPackage updatedPackage = deliveryPackageService.updateDeliveryStatus(id, username, newStatus);
            response.put("status", "success");
            response.put("message", "Package status updated successfully");
            response.put("data", updatedPackage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update package status: {}", e.getMessage());
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
            DeliveryPackage updatedPackage = deliveryPackageService.updateDeliveryLocation(
                id, username, latitude, longitude, location);
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
    public ResponseEntity<?> dropPackage(@PathVariable Long id, @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("Dropping package {} by courier {}", id, username);
            DeliveryPackage droppedPackage = deliveryPackageService.dropDeliveryPackage(id, username);
            response.put("status", "success");
            response.put("message", "Package dropped successfully");
            response.put("data", droppedPackage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to drop package: {}", e.getMessage());
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

    @GetMapping("/reports")
    public ResponseEntity<?> getAllDeliveryReports() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DeliveryReport> reports = deliveryPackageService.getAllDeliveryReports();
            response.put("status", "success");
            response.put("data", reports);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/reports/courier")
    public ResponseEntity<?> getCourierDeliveryReports(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DeliveryReport> reports = deliveryPackageService.getCourierDeliveryReports(username);
            response.put("status", "success");
            response.put("data", reports);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{packageId}/report")
    public ResponseEntity<?> createDeliveryReport(
            @PathVariable Long packageId,
            @Valid @RequestBody DeliveryReport report,
            @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            DeliveryReport savedReport = deliveryPackageService.createDeliveryReport(packageId, report, username);
            response.put("status", "success");
            response.put("data", savedReport);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<?> getDeliveryReportById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            DeliveryReport report = deliveryPackageService.getDeliveryReportById(id);
            response.put("status", "success");
            response.put("data", report);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{packageId}/reports")
    public ResponseEntity<?> getDeliveryReportsByPackage(@PathVariable Long packageId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DeliveryReport> reports = deliveryPackageService.getDeliveryReportsByPackage(packageId);
            response.put("status", "success");
            response.put("data", reports);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/reports/{id}")
    public ResponseEntity<?> updateDeliveryReport(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryReport reportDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            DeliveryReport updatedReport = deliveryPackageService.updateDeliveryReport(id, reportDetails);
            response.put("status", "success");
            response.put("data", updatedReport);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/reports/{id}")
    public ResponseEntity<?> deleteDeliveryReport(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            deliveryPackageService.deleteDeliveryReport(id);
            response.put("status", "success");
            response.put("message", "Report deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
