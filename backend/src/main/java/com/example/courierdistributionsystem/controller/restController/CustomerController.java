package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.exception.CustomerException;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.service.ICustomerService;
import com.example.courierdistributionsystem.service.IUserService;
import com.example.courierdistributionsystem.utils.JwtUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.courierdistributionsystem.service.IDeliveryPackageService;
import com.example.courierdistributionsystem.dto.CreatePackageDto;
import com.example.courierdistributionsystem.dto.DeliveryPackageDto;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer")
@Validated
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IDeliveryPackageService deliveryPackageService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerProfile(@PathVariable @NotNull Long id, @RequestHeader("Authorization") String token) {
        logger.info("Received request to get customer profile with ID: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            logger.debug("Fetching customer profile for ID: {}", id);
            

            String jwtToken = token.replace("Bearer ", "");
            Long tokenUserId = jwtUtils.getUserIdFromToken(jwtToken);
            if (!id.equals(tokenUserId)) {
                logger.warn("Unauthorized access attempt to customer profile. Token ID: {}, Requested ID: {}", tokenUserId, id);
                throw new CustomerException("Unauthorized access to customer profile");
            }
            Optional<Customer> customerOpt = customerService.getCustomerById(id);
            if (customerOpt.isEmpty()) {
                throw new CustomerException("Customer not found");
            }
            Customer customer = customerOpt.get();
            if (customer.getRole() != User.UserRole.CUSTOMER) {
                logger.warn("User with ID {} is not a customer. Role: {}", id, customer.getRole());
                throw new CustomerException("User is not a customer");
            }
            Map<String, Object> customerData = new HashMap<>();
            customerData.put("id", customer.getId());
            customerData.put("username", customer.getUsername());
            customerData.put("email", customer.getEmail());
            customerData.put("phoneNumber", customer.getPhoneNumber());
            customerData.put("defaultAddress", customer.getDefaultAddress());
            customerData.put("role", customer.getRole());
            List<DeliveryPackageDto> activePackages = deliveryPackageService.getCustomerActiveDeliveryPackages(customer.getUsername());
            List<DeliveryPackageDto> recentPackages = deliveryPackageService.getCustomerDeliveryPackages(customer.getUsername())
                .stream()
                .limit(5)
                .toList();
            customerData.put("activePackagesCount", activePackages.size());
            customerData.put("recentPackages", recentPackages);
            response.put("status", "success");
            response.put("data", customerData);
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
    @PostMapping("/createCustomerProfile")
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
            Optional<Customer> customerOpt = customerService.getCustomerById(id);
            if (customerOpt.isEmpty()) {
                throw new CustomerException("Customer not found");
            }
            Customer customer = customerService.updateCustomerProfile(customerOpt.get().getUsername(), customerRequest);
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
    public ResponseEntity<?> createPackage(@Valid @RequestBody CreatePackageDto request, @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        Long userId = jwtUtils.getUserIdFromToken(jwtToken);
        logger.debug("Customer {} (ID: {}) attempting to create new package", username, userId);
        try {
            if (username == null || userId == null) {
                logger.warn("Unauthorized attempt to create package");
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }
            Optional<Customer> customerOpt = customerService.getCustomerById(userId);
            if (customerOpt.isEmpty()) {
                throw new IllegalArgumentException("Customer not found");
            }
            DeliveryPackageDto newPackage = customerService.createDeliveryPackage(username, userId, request);
            logger.info("Customer {} (ID: {}) successfully created package", username, userId);
            return ResponseEntity.ok(newPackage);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid package creation request from customer {} (ID: {}): {}", username, userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating package for customer {} (ID: {}): {}", username, userId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
    @GetMapping("/packages/active")
    public ResponseEntity<?> getActivePackages(@RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token.replace("Bearer ", ""));
        logger.debug("Fetching active packages for customer: {}", username);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized access attempt to active packages");
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            List<DeliveryPackageDto> activePackages = deliveryPackageService.getCustomerActiveDeliveryPackages(username);
            logger.info("Successfully retrieved {} active packages for customer: {}", 
                activePackages.size(), username);
            return ResponseEntity.ok(activePackages);
        } catch (Exception e) {
            logger.error("Error fetching active packages for customer {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/delivery-history")
    public ResponseEntity<?> getDeliveryHistory(@RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token.replace("Bearer ", ""));
        logger.debug("Fetching delivery history for customer: {}", username);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized access attempt to delivery history");
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            List<DeliveryPackageDto> deliveryHistory = deliveryPackageService.getCustomerDeliveryHistory(username);
            logger.info("Successfully retrieved {} delivery history items for customer: {}", 
                deliveryHistory.size(), username);
            return ResponseEntity.ok(deliveryHistory);
        } catch (Exception e) {
            logger.error("Error fetching delivery history for customer {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/packages/{packageId}/cancel")
    public ResponseEntity<?> cancelPackage(@PathVariable Long packageId, @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token.replace("Bearer ", ""));
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

    @GetMapping("/packages/track/{packageId}")
    public ResponseEntity<?> trackPackage(@PathVariable Long packageId, @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token.replace("Bearer ", ""));
        logger.debug("Customer {} attempting to track package {}", username, packageId);
        
        try {
            if (username == null) {
                logger.warn("Unauthorized attempt to track package {}", packageId);
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
            }

            DeliveryPackageDto trackingInfo = deliveryPackageService.trackDeliveryPackage(packageId, username);
            logger.info("Customer {} successfully tracked package {}", username, packageId);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", trackingInfo);
            return ResponseEntity.ok(response);
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