package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.exception.CustomerException;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.service.CustomerService;
import com.example.courierdistributionsystem.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.service.DeliveryPackageService;
import com.example.courierdistributionsystem.dto.CreatePackageRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserService userService;

    @Autowired
    private DeliveryPackageService deliveryPackageService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerProfile(@PathVariable @NotNull Long id) {
        logger.info("Received request to get customer profile with ID: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            logger.debug("Fetching customer profile for ID: {}", id);
            
            Optional<User> user = userService.getUserById(id);
            if (user.isEmpty()) {
                logger.warn("No user found with ID: {}", id);
                throw new CustomerException("Customer not found");
            }

            if (user.get().getRole() != User.UserRole.CUSTOMER) {
                logger.warn("User with ID {} is not a customer. Role: {}", id, user.get().getRole());
                throw new CustomerException("User is not a customer");
            }

            Customer customer = customerService.getCustomerById(id);
            response.put("status", "success");
            response.put("data", customer);
            logger.info("Successfully retrieved customer profile for ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (CustomerException e) {
            logger.warn("Failed to fetch customer profile: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error fetching customer profile: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to fetch customer profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createCustomerProfile(@RequestBody @Valid Map<String, String> customerRequest) {
        logger.info("Received request to create customer profile");
        Map<String, Object> response = new HashMap<>();
        try {
            logger.debug("Creating customer profile with data: {}", customerRequest);
            Customer customer = customerService.createCustomerProfile(customerRequest);
            response.put("status", "success");
            response.put("data", customer);
            logger.info("Successfully created customer profile with ID: {}", customer.getId());
            return ResponseEntity.ok(response);
        } catch (CustomerException e) {
            logger.warn("Failed to create customer profile: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error creating customer profile: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to create customer profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomerProfile(
            @PathVariable @NotNull Long id,
            @RequestBody @Valid Map<String, String> customerRequest) {
        logger.info("Received request to update customer profile with ID: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            logger.debug("Updating customer profile with data: {}", customerRequest);
            Customer customer = customerService.updateCustomerProfile(id, customerRequest);
            response.put("status", "success");
            response.put("data", customer);
            logger.info("Successfully updated customer profile with ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (CustomerException e) {
            logger.warn("Failed to update customer profile: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error updating customer profile: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to update customer profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/packages/create")
    public ResponseEntity<?> createPackage(@Valid @RequestBody CreatePackageRequest request, HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Customer {} attempting to create new package", username);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized attempt to create package");
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            Customer customer = customerService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            DeliveryPackage newPackage = deliveryPackageService.createPackage(request, customer);
            logger.info("Customer {} successfully created package {}", username, newPackage.getPackage_id());
            return ResponseEntity.ok(newPackage);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid package creation request from customer {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating package for customer {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/packages/active")
    public ResponseEntity<?> getActivePackages(HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Fetching active packages for customer: {}", username);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized access attempt to active packages");
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            List<DeliveryPackage> activePackages = deliveryPackageService.getCustomerActiveDeliveryPackages(username);
            logger.info("Successfully retrieved {} active packages for customer: {}", 
                activePackages.size(), username);
            return ResponseEntity.ok(activePackages);
        } catch (Exception e) {
            logger.error("Error fetching active packages for customer {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/delivery-history")
    public ResponseEntity<?> getDeliveryHistory(HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Fetching delivery history for customer: {}", username);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized access attempt to delivery history");
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            List<DeliveryPackage> deliveryHistory = deliveryPackageService.getCustomerDeliveryHistory(username);
            logger.info("Successfully retrieved {} delivery history items for customer: {}", 
                deliveryHistory.size(), username);
            return ResponseEntity.ok(deliveryHistory);
        } catch (Exception e) {
            logger.error("Error fetching delivery history for customer {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/packages/{packageId}/cancel")
    public ResponseEntity<?> cancelPackage(@PathVariable Long packageId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Customer {} attempting to cancel package {}", username, packageId);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized attempt to cancel package {}", packageId);
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            deliveryPackageService.cancelDeliveryPackage(packageId, username);
            logger.info("Customer {} successfully cancelled package {}", username, packageId);
            return ResponseEntity.ok(Map.of("message", "Package cancelled successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid package cancellation request from customer {} for package {}: {}", 
                username, packageId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error cancelling package {} for customer {}: {}", 
                packageId, username, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/packages/{packageId}/track")
    public ResponseEntity<?> trackPackage(@PathVariable Long packageId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        logger.debug("Customer {} attempting to track package {}", username, packageId);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized attempt to track package {}", packageId);
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            Map<String, Object> trackingInfo = deliveryPackageService.trackDeliveryPackage(packageId, username);
            logger.info("Customer {} successfully tracked package {}", username, packageId);
            return ResponseEntity.ok(trackingInfo);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid package tracking request from customer {} for package {}: {}", 
                username, packageId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error tracking package {} for customer {}: {}", 
                packageId, username, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
} 