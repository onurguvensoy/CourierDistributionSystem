package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.DeliveryReportRepository;
import com.example.courierdistributionsystem.repository.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/delivery-reports")
public class DeliveryReportController {

    @Autowired
    private DeliveryReportRepository deliveryReportRepository;

    @Autowired
    private DeliveryPackageRepository packageRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<DeliveryReport> getAllDeliveryReports() {
        return deliveryReportRepository.findAll();
    }

    @GetMapping("/courier")
    public List<DeliveryReport> getCourierDeliveryReports(@RequestParam String username) {
        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return deliveryReportRepository.findByCourier(courier);
    }

    @PostMapping("/package/{packageId}")
    public ResponseEntity<DeliveryReport> createDeliveryReport(
            @PathVariable Long packageId,
            @Valid @RequestBody DeliveryReport report,
            @RequestParam String username) {
        
        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        DeliveryPackage deliveryPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        report.setDeliveryPackage(deliveryPackage);
        report.setCourier(courier);
        report.setCompletionTime(LocalDateTime.now());
        
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.DELIVERED);
        packageRepository.save(deliveryPackage);

        DeliveryReport savedReport = deliveryReportRepository.save(report);
        return ResponseEntity.ok(savedReport);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryReport> getDeliveryReportById(@PathVariable Long id) {
        DeliveryReport report = deliveryReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery report not found"));
        return ResponseEntity.ok(report);
    }

    @GetMapping("/package/{packageId}")
    public ResponseEntity<List<DeliveryReport>> getDeliveryReportsByPackage(@PathVariable Long packageId) {
        DeliveryPackage deliveryPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        List<DeliveryReport> reports = deliveryReportRepository.findByDeliveryPackage(deliveryPackage);
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryReport> updateDeliveryReport(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryReport reportDetails) {
        
        DeliveryReport report = deliveryReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery report not found"));

        report.setDeliveryTime(reportDetails.getDeliveryTime());
        report.setDeliveryNotes(reportDetails.getDeliveryNotes());
        report.setCustomerConfirmation(reportDetails.isCustomerConfirmation());
        report.setDeliveryRating(reportDetails.getDeliveryRating());
        report.setDeliveryPhotoUrl(reportDetails.getDeliveryPhotoUrl());
        report.setSignatureUrl(reportDetails.getSignatureUrl());
        report.setDistanceTraveled(reportDetails.getDistanceTraveled());

        DeliveryReport updatedReport = deliveryReportRepository.save(report);
        return ResponseEntity.ok(updatedReport);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDeliveryReport(@PathVariable Long id) {
        DeliveryReport report = deliveryReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery report not found"));
        deliveryReportRepository.delete(report);
        return ResponseEntity.ok().build();
    }
}
