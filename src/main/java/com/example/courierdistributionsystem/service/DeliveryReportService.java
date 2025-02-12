package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.repository.jpa.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class DeliveryReportService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryReportService.class);
    private static final String REPORTS_CACHE_KEY = "delivery_reports";
    private static final long CACHE_TTL = 3600; // 1 hour in seconds

    @Autowired
    private DeliveryReportRepository deliveryReportRepository;

    @Autowired
    private DeliveryPackageRepository packageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, List<DeliveryReport>> redisTemplate;

    @Cacheable(value = REPORTS_CACHE_KEY)
    public List<DeliveryReport> getAllReports() {
        logger.info("Fetching all delivery reports");
        List<DeliveryReport> reports = deliveryReportRepository.findAll();
        redisTemplate.opsForValue().set(REPORTS_CACHE_KEY, reports, CACHE_TTL, TimeUnit.SECONDS);
        return reports;
    }

    public Optional<DeliveryReport> getReportById(Long id) {
        return deliveryReportRepository.findById(id);
    }

    @CacheEvict(value = REPORTS_CACHE_KEY, allEntries = true)
    @Transactional
    public DeliveryReport generateReport(Long packageId, String adminUsername) {
        logger.info("Generating delivery report for package {} by admin {}", packageId, adminUsername);
        
        Admin admin = (Admin) userService.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        DeliveryPackage deliveryPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Package not found"));

        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Cannot generate report for undelivered package");
        }

        // Calculate delivery metrics
        double distanceTraveled = calculateDistance(deliveryPackage);
        LocalDateTime deliveryTime = deliveryPackage.getDeliveredAt();
        LocalDateTime pickupTime = deliveryPackage.getPickedUpAt();
        
        DeliveryReport report = DeliveryReport.builder()
                .deliveryPackage(deliveryPackage)
                .courier(deliveryPackage.getCourier())
                .admin(admin)
                .deliveryTime(deliveryTime)
                .completionTime(LocalDateTime.now())
                .distanceTraveled(distanceTraveled)
                .timestamp(LocalDateTime.now())
                .reportType("DELIVERY_COMPLETION")
                .content(generateReportContent(deliveryPackage, distanceTraveled, pickupTime, deliveryTime))
                .status("COMPLETED")
                .build();

        return deliveryReportRepository.save(report);
    }

    private String generateReportContent(DeliveryPackage pkg, double distance, LocalDateTime pickupTime, LocalDateTime deliveryTime) {
        StringBuilder content = new StringBuilder();
        content.append("Delivery Report Summary:\n\n");
        content.append("Package ID: ").append(pkg.getPackage_id()).append("\n");
        content.append("Tracking Number: ").append(pkg.getTrackingNumber()).append("\n");
        content.append("Customer: ").append(pkg.getCustomer().getUsername()).append("\n");
        content.append("Courier: ").append(pkg.getCourier().getUsername()).append("\n");
        content.append("Pickup Address: ").append(pkg.getPickupAddress()).append("\n");
        content.append("Delivery Address: ").append(pkg.getDeliveryAddress()).append("\n");
        content.append("Distance Traveled: ").append(String.format("%.2f", distance)).append(" km\n");
        content.append("Pickup Time: ").append(pickupTime).append("\n");
        content.append("Delivery Time: ").append(deliveryTime).append("\n");
    
        return content.toString();
    }

    private double calculateDistance(DeliveryPackage deliveryPackage) {
        // In a real application, this would calculate the actual distance traveled
        // For now, we'll return a sample distance based on package ID
        return 5.0 + (deliveryPackage.getPackage_id() % 10);
    }

    public List<DeliveryReport> getReportsByPackage(Long packageId) {
        DeliveryPackage deliveryPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Package not found"));
        return deliveryReportRepository.findByDeliveryPackage(deliveryPackage);
    }

    public List<DeliveryReport> getReportsByCourier(String username) {
        return deliveryReportRepository.findByCourier_Username(username);
    }

    @CacheEvict(value = REPORTS_CACHE_KEY, allEntries = true)
    @Transactional
    public void deleteReport(Long id) {
        deliveryReportRepository.deleteById(id);
    }
} 