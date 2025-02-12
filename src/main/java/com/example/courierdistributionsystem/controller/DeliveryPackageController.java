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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private String getUsername(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No session attributes found");
        }
        String username = (String) attributes.get("username");
        if (username == null) {
            throw new IllegalStateException("No username found in session");
        }
        return username;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPackage(@Valid @RequestBody CreatePackageRequest request, HttpSession session) {
        logger.debug("Received request to create new package from session: {}", session.getId());
        try {
            String username = (String) session.getAttribute("username");
            if (username == null || username.trim().isEmpty()) {
                logger.warn("Attempt to create package without authentication");
                throw new IllegalArgumentException("User not authenticated");
            }

            if (!username.equals(request.getUsername())) {
                logger.warn("Username mismatch in package creation. Session user: {}, Request user: {}", 
                    username, request.getUsername());
                throw new IllegalArgumentException("Username mismatch");
            }

            Optional<Customer> customer = customerService.findByUsername(username);
            if (customer.isEmpty()) {
                logger.warn("Customer not found for username: {}", username);
                throw new IllegalArgumentException("Customer not found");
            }

            DeliveryPackage newPackage = deliveryPackageService.createPackage(request, customer.get());
            logger.info("Successfully created package with ID: {} for customer: {}", 
                newPackage.getPackage_id(), username);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Package created successfully");
            response.put("package_id", newPackage.getPackage_id());
            response.put("tracking_number", newPackage.getTrackingNumber());
            response.put("pickup_address", newPackage.getPickupAddress());
            response.put("delivery_address", newPackage.getDeliveryAddress());
            response.put("status", newPackage.getStatus().toString());
            response.put("created_at", newPackage.getCreatedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to create package: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @MessageMapping("/package/track")
    public void trackPackage(@Payload Map<String, Object> trackingRequest, SimpMessageHeaderAccessor headerAccessor) {
        String username = null;
        Long packageId = null;
        try {
            username = getUsername(headerAccessor);
            packageId = Long.valueOf(trackingRequest.get("packageId").toString());
            logger.debug("Received tracking request for package: {} from user: {}", packageId, username);
            
            deliveryPackageService.trackDeliveryPackage(packageId, username);
            logger.info("Successfully processed tracking request for package: {} from user: {}", packageId, username);
        } catch (Exception e) {
            logger.error("Failed to track package: {} for user: {}. Error: {}", 
                packageId, username, e.getMessage(), e);
        }
    }

    @MessageMapping("/package/status/update")
    public void updatePackageStatus(@Payload Map<String, Object> statusUpdate, SimpMessageHeaderAccessor headerAccessor) {
        String username = null;
        Long packageId = null;
        String status = null;
        try {
            username = getUsername(headerAccessor);
            packageId = Long.valueOf(statusUpdate.get("packageId").toString());
            status = (String) statusUpdate.get("status");
            
            logger.debug("Received status update request for package: {} to status: {} from user: {}", 
                packageId, status, username);
            
            DeliveryPackage.DeliveryStatus newStatus = DeliveryPackage.DeliveryStatus.valueOf(status.toUpperCase());
            DeliveryPackage updatedPackage = deliveryPackageService.updateDeliveryStatus(packageId, username, newStatus);
            
            // Notify the customer about the status change
            messagingTemplate.convertAndSendToUser(
                updatedPackage.getCustomer().getUsername(),
                "/queue/package/status",
                Map.of(
                    "packageId", packageId,
                    "status", newStatus,
                    "message", "Package status updated to " + newStatus
                )
            );

            // Broadcast to available packages topic if the package becomes available again
            if (newStatus == DeliveryPackage.DeliveryStatus.PENDING) {
                messagingTemplate.convertAndSend(
                    "/topic/packages/available",
                    Map.of(
                        "packageId", packageId,
                        "status", newStatus,
                        "message", "New package available for delivery"
                    )
                );
            }
            
            logger.info("Successfully updated status for package: {} to: {} by user: {}", 
                packageId, newStatus, username);
        } catch (Exception e) {
            logger.error("Failed to update package status: {} to: {} for user: {}. Error: {}", 
                packageId, status, username, e.getMessage(), e);
            String errorUsername = getUsername(headerAccessor);
            messagingTemplate.convertAndSendToUser(
                errorUsername,
                "/queue/errors",
                Map.of("error", "Failed to update package status: " + e.getMessage())
            );
        }
    }

    @MessageMapping("/package/take")
    public void takePackage(@Payload Map<String, Object> request, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = getUsername(headerAccessor);
            Long packageId = Long.valueOf(request.get("packageId").toString());
            
            DeliveryPackage updatedPackage = deliveryPackageService.takeDeliveryPackage(packageId, username);
            
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/package/assigned",
                Map.of(
                    "packageId", packageId,
                    "message", "Package successfully assigned"
                )
            );
            messagingTemplate.convertAndSendToUser(
                updatedPackage.getCustomer().getUsername(),
                "/queue/package/status",
                Map.of(
                    "packageId", packageId,
                    "status", "ASSIGNED",
                    "courierUsername", username,
                    "message", "Your package has been assigned to a courier"
                )
            );

            messagingTemplate.convertAndSend("/topic/packages/available", 
                deliveryPackageService.getAvailableDeliveryPackages());
                
        } catch (Exception e) {
            logger.error("Failed to take package: {}", e.getMessage());
            String errorUsername = getUsername(headerAccessor);
            messagingTemplate.convertAndSendToUser(
                errorUsername,
                "/queue/errors",
                Map.of("error", "Failed to take package: " + e.getMessage())
            );
        }
    }

    @MessageMapping("/package/drop")
    public void dropPackage(@Payload Map<String, Object> request, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = getUsername(headerAccessor);
            Long packageId = Long.valueOf(request.get("packageId").toString());
            deliveryPackageService.dropDeliveryPackage(packageId, username);
        } catch (Exception e) {
            logger.error("Failed to drop package: {}", e.getMessage());
            String errorUsername = getUsername(headerAccessor);
            messagingTemplate.convertAndSendToUser(
                errorUsername,
                "/queue/errors",
                Map.of("error", "Failed to drop package: " + e.getMessage())
            );
        }
    }

    @MessageMapping("/packages/available")
    public void getAvailablePackages(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = getUsername(headerAccessor);
            List<DeliveryPackage> packages = deliveryPackageService.getAvailableDeliveryPackages();
    
            messagingTemplate.convertAndSendToUser(username, "/queue/packages/available", packages);
        } catch (Exception e) {
            logger.error("Failed to get available packages: {}", e.getMessage());
        }
    }

    @MessageMapping("/packages/active")
    public void getActivePackages(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = getUsername(headerAccessor);
            List<DeliveryPackage> packages = deliveryPackageService.getCourierActiveDeliveryPackages(username);

            messagingTemplate.convertAndSendToUser(username, "/queue/packages/active", packages);
        } catch (Exception e) {
            logger.error("Failed to get active packages: {}", e.getMessage());
            try {
                String errorUsername = getUsername(headerAccessor);
                messagingTemplate.convertAndSendToUser(errorUsername, "/queue/errors", 
                    Map.of("error", "Failed to get active packages: " + e.getMessage()));
            } catch (Exception ex) {
                logger.error("Could not send error message: {}", ex.getMessage());
            }
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
      
            String username = (String) session.getAttribute("username");
            if (username == null) {
                throw new IllegalArgumentException("User not authenticated");
            }


            deliveryRequest.put("username", username);

        
            String[] requiredFields = {"pickupAddress", "deliveryAddress", "weight", "description"};
            for (String field : requiredFields) {
                if (!deliveryRequest.containsKey(field) || deliveryRequest.get(field).trim().isEmpty()) {
                    throw new IllegalArgumentException("Missing required field: " + field);
                }
            }


            Customer customer = customerService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

    
            DeliveryPackage newPackage = DeliveryPackage.builder()
                .customer(customer)
                .pickupAddress(deliveryRequest.get("pickupAddress"))
                .deliveryAddress(deliveryRequest.get("deliveryAddress"))
                .weight(Double.parseDouble(deliveryRequest.get("weight")))
                .description(deliveryRequest.get("description"))
                .specialInstructions(deliveryRequest.getOrDefault("specialInstructions", ""))
                .build();

           
            DeliveryPackage savedPackage = deliveryPackageService.createDeliveryPackage(newPackage);
            
            response.put("status", "success");
            response.put("message", "Delivery package created successfully");
            response.put("data", savedPackage);
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
