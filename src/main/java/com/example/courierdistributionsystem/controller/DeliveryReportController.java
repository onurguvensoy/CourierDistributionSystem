package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.exception.DeliveryReportException;
import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.repository.DeliveryReportRepository;
import com.example.courierdistributionsystem.repository.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.CourierRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery-reports")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated
public class DeliveryReportController {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryReportController.class);

    @Autowired
    private DeliveryReportRepository deliveryReportRepository;

    @Autowired
    private DeliveryPackageRepository packageRepository;

    @Autowired
    private CourierRepository courierRepository;

    @GetMapping
    public ResponseEntity<?> getAllDeliveryReports() {
        logger.debug("Fetching all delivery reports");
        try {
            List<DeliveryReport> reports = deliveryReportRepository.findAll();
            logger.info("Successfully retrieved {} delivery reports", reports.size());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", reports
            ));
        } catch (Exception e) {
            logger.error("Failed to fetch delivery reports: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to fetch delivery reports"
            ));
        }
    }

    @GetMapping("/courier")
    public ResponseEntity<?> getCourierDeliveryReports(@RequestParam @NotNull String username) {
        logger.debug("Fetching delivery reports for courier: {}", username);
        try {
            Courier courier = courierRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("Courier not found with username: {}", username);
                        return new DeliveryReportException("Courier not found");
                    });

            List<DeliveryReport> reports = deliveryReportRepository.findByCourier(courier);
            logger.info("Successfully retrieved {} delivery reports for courier: {}", reports.size(), username);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", reports
            ));
        } catch (DeliveryReportException e) {
            logger.warn("Failed to fetch courier delivery reports: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Failed to fetch courier delivery reports: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to fetch courier delivery reports"
            ));
        }
    }

    @PostMapping("/package/{packageId}")
    public ResponseEntity<?> createDeliveryReport(
            @PathVariable @NotNull Long packageId,
            @Valid @RequestBody DeliveryReport report,
            @RequestParam @NotNull String username) {
        
        logger.debug("Creating delivery report for package {} by courier {}", packageId, username);
        try {
            validateDeliveryReport(report);

            Courier courier = courierRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("Courier not found with username: {}", username);
                        return new DeliveryReportException("Courier not found");
                    });
            
            DeliveryPackage deliveryPackage = packageRepository.findById(packageId)
                    .orElseThrow(() -> {
                        logger.warn("Package not found with ID: {}", packageId);
                        return new DeliveryReportException("Package not found");
                    });

            report.setDeliveryPackage(deliveryPackage);
            report.setCourier(courier);
            report.setCompletionTime(LocalDateTime.now());
            
            deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.DELIVERED);
            packageRepository.save(deliveryPackage);

            DeliveryReport savedReport = deliveryReportRepository.save(report);
            logger.info("Successfully created delivery report with ID: {} for package: {}", savedReport.getId(), packageId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", savedReport
            ));
        } catch (DeliveryReportException e) {
            logger.warn("Failed to create delivery report: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Failed to create delivery report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to create delivery report"
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeliveryReportById(@PathVariable @NotNull Long id) {
        logger.debug("Fetching delivery report with ID: {}", id);
        try {
            DeliveryReport report = deliveryReportRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Delivery report not found with ID: {}", id);
                        return new DeliveryReportException("Delivery report not found");
                    });
            logger.info("Successfully retrieved delivery report with ID: {}", id);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", report
            ));
        } catch (DeliveryReportException e) {
            logger.warn("Failed to fetch delivery report: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Failed to fetch delivery report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to fetch delivery report"
            ));
        }
    }

    @GetMapping("/package/{packageId}")
    public ResponseEntity<?> getDeliveryReportsByPackage(@PathVariable @NotNull Long packageId) {
        logger.debug("Fetching delivery reports for package: {}", packageId);
        try {
            DeliveryPackage deliveryPackage = packageRepository.findById(packageId)
                    .orElseThrow(() -> {
                        logger.warn("Package not found with ID: {}", packageId);
                        return new DeliveryReportException("Package not found");
                    });

            List<DeliveryReport> reports = deliveryReportRepository.findByDeliveryPackage(deliveryPackage);
            logger.info("Successfully retrieved {} delivery reports for package: {}", reports.size(), packageId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", reports
            ));
        } catch (DeliveryReportException e) {
            logger.warn("Failed to fetch package delivery reports: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Failed to fetch package delivery reports: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to fetch package delivery reports"
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDeliveryReport(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody DeliveryReport reportDetails) {
        
        logger.debug("Updating delivery report with ID: {}", id);
        try {
            validateDeliveryReport(reportDetails);

            DeliveryReport report = deliveryReportRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Delivery report not found with ID: {}", id);
                        return new DeliveryReportException("Delivery report not found");
                    });

            updateReportFields(report, reportDetails);
            DeliveryReport updatedReport = deliveryReportRepository.save(report);
            logger.info("Successfully updated delivery report with ID: {}", id);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", updatedReport
            ));
        } catch (DeliveryReportException e) {
            logger.warn("Failed to update delivery report: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Failed to update delivery report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to update delivery report"
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDeliveryReport(@PathVariable @NotNull Long id) {
        logger.debug("Deleting delivery report with ID: {}", id);
        try {
            DeliveryReport report = deliveryReportRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Delivery report not found with ID: {}", id);
                        return new DeliveryReportException("Delivery report not found");
                    });

            deliveryReportRepository.delete(report);
            logger.info("Successfully deleted delivery report with ID: {}", id);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Delivery report deleted successfully"
            ));
        } catch (DeliveryReportException e) {
            logger.warn("Failed to delete delivery report: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Failed to delete delivery report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to delete delivery report"
            ));
        }
    }

    private void validateDeliveryReport(DeliveryReport report) {
        Map<String, String> errors = new HashMap<>();

        if (report.getDeliveryTime() != null && report.getDeliveryTime().isAfter(LocalDateTime.now())) {
            errors.put("deliveryTime", "Delivery time cannot be in the future");
        }

        if (report.getDeliveryRating() != null && (report.getDeliveryRating() < 1 || report.getDeliveryRating() > 5)) {
            errors.put("deliveryRating", "Delivery rating must be between 1 and 5");
        }

        if (report.getDistanceTraveled() != null && report.getDistanceTraveled() < 0) {
            errors.put("distanceTraveled", "Distance traveled cannot be negative");
        }

        if (!errors.isEmpty()) {
            logger.warn("Delivery report validation failed: {}", errors);
            throw new DeliveryReportException("Validation failed: " + errors);
        }
    }

    private void updateReportFields(DeliveryReport report, DeliveryReport reportDetails) {
        report.setDeliveryTime(reportDetails.getDeliveryTime());
        report.setDeliveryNotes(reportDetails.getDeliveryNotes());
        report.setCustomerConfirmation(reportDetails.isCustomerConfirmation());
        report.setDeliveryRating(reportDetails.getDeliveryRating());
        report.setDeliveryPhotoUrl(reportDetails.getDeliveryPhotoUrl());
        report.setSignatureUrl(reportDetails.getSignatureUrl());
        report.setDistanceTraveled(reportDetails.getDistanceTraveled());
    }
}
