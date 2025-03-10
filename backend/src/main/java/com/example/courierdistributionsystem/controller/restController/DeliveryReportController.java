package com.example.courierdistributionsystem.controller.restController;
import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.service.IDeliveryReportService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery-reports")
@Validated
public class DeliveryReportController {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryReportController.class);

    @Autowired
    private IDeliveryReportService deliveryReportService;

    @GetMapping
    public ResponseEntity<?> getAllDeliveryReports() {
        logger.debug("Fetching all delivery reports");
        try {
            List<DeliveryReport> reports = deliveryReportService.getAllReports();
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
            List<DeliveryReport> reports = deliveryReportService.getReportsByCourier(username);
            logger.info("Successfully retrieved {} delivery reports for courier: {}", reports.size(), username);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", reports
            ));
        } catch (Exception e) {
            logger.error("Failed to fetch courier delivery reports: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to fetch courier delivery reports"
            ));
        }
    }

    @PostMapping("/generate/{packageId}")
    public ResponseEntity<?> generateReport(
            @PathVariable @NotNull Long packageId,
            @RequestParam @NotNull String username) {
        
        logger.debug("Generating report for package {} by admin {}", packageId, username);
        try {
            DeliveryReport report = deliveryReportService.generateReport(packageId, username);
            logger.info("Successfully generated report with ID: {} for package: {}", report.getId(), packageId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", report
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Failed to generate report: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Failed to generate report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to generate report"
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeliveryReportById(@PathVariable @NotNull Long id) {
        logger.debug("Fetching delivery report with ID: {}", id);
        try {
            return deliveryReportService.getReportById(id)
                .map(report -> ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", report
                )))
                .orElse(ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Delivery report not found"
                )));
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
            List<DeliveryReport> reports = deliveryReportService.getReportsByPackage(packageId);
            logger.info("Successfully retrieved {} delivery reports for package: {}", reports.size(), packageId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", reports
            ));
        } catch (IllegalArgumentException e) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDeliveryReport(@PathVariable @NotNull Long id) {
        logger.debug("Deleting delivery report with ID: {}", id);
        try {
            deliveryReportService.deleteReport(id);
            logger.info("Successfully deleted delivery report with ID: {}", id);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Delivery report deleted successfully"
            ));
        } catch (Exception e) {
            logger.error("Failed to delete delivery report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to delete delivery report"
            ));
        }
    }
}
