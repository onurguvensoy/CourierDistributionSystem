package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.repository.jpa.*;
import com.example.courierdistributionsystem.socket.WebSocketService;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ViewService {
    private static final Logger logger = LoggerFactory.getLogger(ViewService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DeliveryPackageRepository packageRepository;

    @Autowired
    private DeliveryPackageService deliveryPackageService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private UserRepository userRepository;

    public User getUserByUsername(String username) {
        if (username == null) {
            throw new RuntimeException("Username cannot be null");
        }

        Customer customer = customerRepository.findByUsername(username).orElse(null);
        if (customer != null) {
            return customer;
        }
        
        Courier courier = courierRepository.findByUsername(username).orElse(null);
        if (courier != null) {
            return courier;
        }
        
        Admin admin = adminRepository.findByUsername(username).orElse(null);
        if (admin != null) {
            return admin;
        }

        throw new RuntimeException("User not found: " + username);
    }

    public String getDashboardRedirect(String username) {
        if (username == null) {
            logger.error("Username is null in getDashboardRedirect");
            return "redirect:/auth/login";
        }

        try {
            User user = getUserByUsername(username);
            if (user instanceof Admin) {
                logger.debug("Redirecting admin user {} to admin dashboard", username);
                return "redirect:/admin/dashboard";
            } else if (user instanceof Courier) {
                logger.debug("Redirecting courier user {} to courier dashboard", username);
                return "redirect:/courier/dashboard";
            } else if (user instanceof Customer) {
                logger.debug("Redirecting customer user {} to customer dashboard", username);
                return "redirect:/customer/dashboard";
            } else {
                logger.error("Unknown user type for user {}", username);
                throw new RuntimeException("Unknown user type");
            }
        } catch (Exception e) {
            logger.error("Error in getDashboardRedirect for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Error determining user role: " + e.getMessage());
        }
    }

    public boolean isValidRole(User user, String role) {
        if (user == null || role == null) {
            return false;
        }

        return switch (role.toUpperCase()) {
            case "ADMIN" -> user instanceof Admin;
            case "COURIER" -> user instanceof Courier;
            case "CUSTOMER" -> user instanceof Customer;
            default -> false;
        };
    }

    public List<DeliveryPackage> getAvailablePackages() {
        return packageRepository.findByStatusAndCourierIsNull(DeliveryPackage.DeliveryStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<DeliveryPackage> getActiveDeliveries(User courier) {
        if (!(courier instanceof Courier)) {
            throw new IllegalArgumentException("User must be a courier");
        }

        return packageRepository.findByCourierAndStatusIn(
            (Courier) courier, 
            List.of(DeliveryPackage.DeliveryStatus.ASSIGNED, 
                   DeliveryPackage.DeliveryStatus.PICKED_UP, 
                   DeliveryPackage.DeliveryStatus.IN_TRANSIT)
        );
    }

    @Transactional(readOnly = true)
    public List<DeliveryPackage> getCustomerPackages(User customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        
        if (customer.getRole() != User.UserRole.CUSTOMER) {
            throw new IllegalStateException("Only customers can view their packages");
        }
        
        return packageRepository.findByCustomer((Customer) customer);
    }

    @Transactional
    public void takeDeliveryPackage(Long packageId, String username) {
        DeliveryPackage deliveryPackage = deliveryPackageService.getDeliveryPackageById(packageId);
        Courier courier = courierRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Courier not found"));

        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            throw new RuntimeException("Package is not available for pickup");
        }

        deliveryPackage.setCourier(courier);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.ASSIGNED);
        packageRepository.save(deliveryPackage);
        
        webSocketService.notifyDeliveryStatusUpdate(deliveryPackage);
    }

    @Transactional
    public void updateDeliveryStatus(Long packageId, String username, String status) {
        DeliveryPackage deliveryPackage = deliveryPackageService.getDeliveryPackageById(packageId);
        Courier courier = courierRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Courier not found"));

        if (!deliveryPackage.getCourier().equals(courier)) {
            throw new RuntimeException("Package is not assigned to this courier");
        }

        try {
            DeliveryPackage.DeliveryStatus newStatus = DeliveryPackage.DeliveryStatus.valueOf(status.toUpperCase());
            deliveryPackage.setStatus(newStatus);
            packageRepository.save(deliveryPackage);
            
            webSocketService.notifyDeliveryStatusUpdate(deliveryPackage);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }

    @Transactional
    public void dropDeliveryPackage(Long packageId, String username) {
        DeliveryPackage deliveryPackage = deliveryPackageService.getDeliveryPackageById(packageId);
        Courier courier = courierRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Courier not found"));

        if (!deliveryPackage.getCourier().equals(courier)) {
            throw new RuntimeException("Package is not assigned to this courier");
        }

        deliveryPackage.setCourier(null);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.PENDING);
        packageRepository.save(deliveryPackage);
        
        webSocketService.notifyDeliveryStatusUpdate(deliveryPackage);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerDashboardStats(User user) {
        if (!(user instanceof Customer)) {
            throw new IllegalArgumentException("User must be a customer");
        }
        Customer customer = (Customer) user;
        
        List<DeliveryPackage> allPackages = packageRepository.findByCustomer(customer);
        
        List<DeliveryPackage> activePackages = allPackages.stream()
            .filter(pkg -> !pkg.getStatus().equals(DeliveryPackage.DeliveryStatus.DELIVERED) 
                && !pkg.getStatus().equals(DeliveryPackage.DeliveryStatus.CANCELLED))
            .toList();

        List<DeliveryPackage> completedPackages = allPackages.stream()
            .filter(pkg -> pkg.getStatus().equals(DeliveryPackage.DeliveryStatus.DELIVERED) 
                || pkg.getStatus().equals(DeliveryPackage.DeliveryStatus.CANCELLED))
            .peek(pkg -> {
                if (pkg.getDeliveredAt() != null) {
                    pkg.setFormattedDeliveryDate(pkg.getDeliveredAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            })
            .toList();

        long deliveredCount = allPackages.stream()
            .filter(pkg -> pkg.getStatus().equals(DeliveryPackage.DeliveryStatus.DELIVERED))
            .count();

        

        Map<String, Object> stats = new HashMap<>();
        stats.put("activePackages", activePackages);
        stats.put("completedPackages", completedPackages);
        stats.put("totalPackages", allPackages.size());
        stats.put("activeShipments", activePackages.size());
        stats.put("deliveredPackages", deliveredCount);

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerProfileStats(Customer customer) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalPackages = customer.getPackages().size();
        long deliveredPackages = customer.getPackages().stream()
            .filter(p -> p.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED)
            .count();
        long activeShipments = customer.getPackages().stream()
            .filter(p -> p.getStatus() != DeliveryPackage.DeliveryStatus.DELIVERED 
                && p.getStatus() != DeliveryPackage.DeliveryStatus.CANCELLED)
            .count();
        stats.put("totalPackages", totalPackages);
        stats.put("deliveredPackages", deliveredPackages);
        stats.put("activeShipments", activeShipments);

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCourierProfileStats(Courier courier) {
        Map<String, Object> stats = new HashMap<>();
        
        List<DeliveryPackage> deliveries = courier.getDeliveries();
        long totalDeliveries = deliveries.size();
        long completedDeliveries = deliveries.stream()
            .filter(d -> d.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED)
            .count();
        long activeDeliveries = deliveries.stream()
            .filter(d -> d.getStatus() != DeliveryPackage.DeliveryStatus.DELIVERED 
                && d.getStatus() != DeliveryPackage.DeliveryStatus.CANCELLED)
            .count();


        stats.put("totalDeliveries", totalDeliveries);
        stats.put("completedDeliveries", completedDeliveries);
        stats.put("activeDeliveries", activeDeliveries);

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminProfileStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long customerCount = customerRepository.count();
        long courierCount = courierRepository.count();
        long totalPackages = packageRepository.count();
        long activePackages = packageRepository.findAll().stream()
            .filter(p -> p.getStatus() != DeliveryPackage.DeliveryStatus.DELIVERED)
            .count();

        stats.put("totalUsers", totalUsers);
        stats.put("customerCount", customerCount);
        stats.put("courierCount", courierCount);
        stats.put("totalPackages", totalPackages);
        stats.put("activePackages", activePackages);

        return stats;
    }
}