package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.dto.CreatePackageDto;
import com.example.courierdistributionsystem.dto.DeliveryPackageDto;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.service.IDeliveryPackageService;
import com.example.courierdistributionsystem.service.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packages")
public class DeliveryPackageController {

    private final IDeliveryPackageService deliveryPackageService;
    private final ICustomerService customerService;

    @Autowired
    public DeliveryPackageController(IDeliveryPackageService deliveryPackageService, ICustomerService customerService) {
        this.deliveryPackageService = deliveryPackageService;
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<DeliveryPackageDto> createDeliveryPackage(
            @RequestHeader("X-Username") String username,
            @RequestBody CreatePackageDto request) {
        DeliveryPackageDto createdPackage = customerService.createDeliveryPackage(username, request);
        return ResponseEntity.ok(createdPackage);
    }

    @GetMapping("/available")
    public ResponseEntity<List<DeliveryPackageDto>> getAvailableDeliveryPackages() {
        List<DeliveryPackageDto> packages = deliveryPackageService.getAvailableDeliveryPackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryPackageDto> getDeliveryPackageById(@PathVariable Long id) {
        return deliveryPackageService.getDeliveryPackageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/courier/active")
    public ResponseEntity<List<DeliveryPackageDto>> getCourierActiveDeliveryPackages(
            @RequestHeader("X-Username") String username) {
        List<DeliveryPackageDto> packages = deliveryPackageService.getCourierActiveDeliveryPackages(username);
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/customer")
    public ResponseEntity<List<DeliveryPackageDto>> getCustomerDeliveryPackages(
            @RequestHeader("X-Username") String username) {
        List<DeliveryPackageDto> packages = deliveryPackageService.getCustomerDeliveryPackages(username);
        return ResponseEntity.ok(packages);
    }

    @PostMapping("/{id}/take")
    public ResponseEntity<DeliveryPackageDto> takeDeliveryPackage(
            @PathVariable Long id,
            @RequestHeader("X-Username") String username) {
        DeliveryPackageDto updatedPackage = deliveryPackageService.takeDeliveryPackage(id, username);
        return ResponseEntity.ok(updatedPackage);
    }

    @PostMapping("/{id}/drop")
    public ResponseEntity<DeliveryPackageDto> dropDeliveryPackage(
            @PathVariable Long id,
            @RequestHeader("X-Username") String username) {
        DeliveryPackageDto updatedPackage = deliveryPackageService.dropDeliveryPackage(id, username);
        return ResponseEntity.ok(updatedPackage);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryPackageDto> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestHeader("X-Username") String username,
            @RequestBody Map<String, String> request) {
        DeliveryPackage.DeliveryStatus status = DeliveryPackage.DeliveryStatus.valueOf(request.get("status"));
        DeliveryPackageDto updatedPackage = deliveryPackageService.updateDeliveryStatus(id, username, status);
        return ResponseEntity.ok(updatedPackage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeliveryPackage(@PathVariable Long id) {
        deliveryPackageService.deleteDeliveryPackage(id);
        return ResponseEntity.ok().build();
    }
}
