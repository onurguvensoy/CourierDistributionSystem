package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.dto.DeliveryPackageDto;
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
import com.example.courierdistributionsystem.service.IUserService;
import com.example.courierdistributionsystem.service.IDeliveryPackageService;
import com.example.courierdistributionsystem.service.IDeliveryReportService;
import com.example.courierdistributionsystem.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private IDeliveryPackageService deliveryPackageService;


    @Autowired
    private IDeliveryReportService deliveryReportService;



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
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            logger.info("Deleting user with ID: {}", id);
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
            userService.deleteUser(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
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
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Update user fields
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        
        User updatedUser = userService.updateUser(existingUser);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/users/{id}/profile")
    public ResponseEntity<User> getUserProfile(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(user);
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
            
            List<DeliveryPackageDto> allPackages = deliveryPackageService.getAllDeliveryPackages();
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
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
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