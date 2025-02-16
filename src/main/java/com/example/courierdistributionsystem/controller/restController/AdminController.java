package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DeliveryPackageService deliveryPackageService;


    @Autowired
    private DeliveryReportService deliveryReportService;

    // User Management Endpoints
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        try {
            validateAdminSession(session);
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        try {
            logger.info("Fetching user with ID: {}", id);
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new UserService.UserNotFoundException("User not found with ID: " + id));
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User fetched successfully");
            response.put("data", user);
            
            return ResponseEntity.ok(response);
        } catch (UserService.UserNotFoundException e) {
            logger.warn("User fetch failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Unexpected error during user fetch", e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            logger.info("Deleting user with ID: {}", id);
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new UserService.UserNotFoundException("User not found with ID: " + id));
            userService.deleteUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (UserService.UserNotFoundException e) {
            logger.warn("User deletion failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Unexpected error during user deletion", e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        try {
            logger.info("Updating user with ID: {}", id);
            User updatedUser = userService.editUser(id, updates);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User updated successfully");
            response.put("data", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (UserService.UserNotFoundException e) {
            logger.warn("User update failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("User update failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error during user update", e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/packages")
    public ResponseEntity<?> getAllPackages(HttpSession session) {
        try {
            validateAdminSession(session);
            return ResponseEntity.ok(deliveryPackageService.getAllDeliveryPackages());
        } catch (Exception e) {
            return handleError(e);
        }
    }

    @GetMapping("/packages/{id}")
    public ResponseEntity<?> getPackage(@PathVariable Long id, HttpSession session) {
        try {
            validateAdminSession(session);
            return ResponseEntity.ok(deliveryPackageService.getDeliveryPackageById(id));
        } catch (Exception e) {
            return handleError(e);
        }
    }

    @DeleteMapping("/packages/{id}")
    public ResponseEntity<?> deletePackage(@PathVariable Long id, HttpSession session) {
        try {
            validateAdminSession(session);
            deliveryPackageService.getDeliveryPackageById(id);
            return ResponseEntity.ok(Map.of("message", "Package deleted successfully"));
        } catch (Exception e) {
            return handleError(e);
        }
    }

    @PutMapping("/packages/{id}")
    public ResponseEntity<?> updatePackage(@PathVariable Long id, @RequestBody Map<String, String> updates, HttpSession session) {
        try {
            validateAdminSession(session);
            return ResponseEntity.ok(deliveryPackageService.updateDeliveryPackage(id, updates));
        } catch (Exception e) {
            return handleError(e);
        }
    }


    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(HttpSession session) {
        try {
            validateAdminSession(session);
            Map<String, Object> stats = new HashMap<>();
            
            Map<String, Object> userStats = userService.getUserStats();
            stats.putAll(userStats);
            
            List<DeliveryPackage> allPackages = deliveryPackageService.getAllDeliveryPackages();
            stats.put("totalPackages", allPackages.size());
            stats.put("pendingPackages", allPackages.stream()
                .filter(p -> p.getStatus() == DeliveryPackage.DeliveryStatus.PENDING)
                .count());
            stats.put("activeDeliveries", allPackages.stream()
                .filter(p -> p.getStatus() == DeliveryPackage.DeliveryStatus.IN_PROGRESS)
                .count());
            stats.put("deliveredPackages", allPackages.stream()
                .filter(p -> p.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED)
                .count());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    @GetMapping("/reports")
    public String showReportsPage(Model model) {
        List<DeliveryReport> reports = deliveryReportService.getAllReports();
        model.addAttribute("reports", reports);
        return "reports";
    }


    private void validateAdminSession(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new IllegalStateException("No user logged in");
        }
        
        User user = userService.findByUsername(username)
        .orElseThrow(() -> new UserService.UserNotFoundException("User not found with username: " + username));
        if (!(user instanceof Admin)) {
            throw new IllegalStateException("User is not an admin");
        }
    }

    private ResponseEntity<?> handleError(Exception e) {
        logger.error("Admin operation failed: {}", e.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
} 