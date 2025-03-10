package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.dto.CreatePackageDto;
import com.example.courierdistributionsystem.dto.DeliveryPackageDto;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.service.IDeliveryPackageService;
import com.example.courierdistributionsystem.service.ICustomerService;
import com.example.courierdistributionsystem.utils.JwtUtils;
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
    private final JwtUtils jwtUtils;

    @Autowired
    public DeliveryPackageController(IDeliveryPackageService deliveryPackageService, 
                                   ICustomerService customerService,
                                   JwtUtils jwtUtils) {
        this.deliveryPackageService = deliveryPackageService;
        this.customerService = customerService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping
    public ResponseEntity<DeliveryPackageDto> createDeliveryPackage(
            @RequestHeader("Authorization") String token,
            @RequestBody CreatePackageDto request) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        Long userId = jwtUtils.getUserIdFromToken(jwtToken);
        DeliveryPackageDto createdPackage = customerService.createDeliveryPackage(username, userId, request);
        return ResponseEntity.ok(createdPackage);
    }

    @GetMapping("/available")
    public ResponseEntity<List<DeliveryPackageDto>> getAvailableDeliveryPackages(
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        jwtUtils.validateToken(jwtToken); // Validate token but we don't need the user info for this endpoint
        List<DeliveryPackageDto> packages = deliveryPackageService.getAvailableDeliveryPackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryPackageDto> getDeliveryPackageById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        jwtUtils.validateToken(jwtToken); // Validate token but we don't need the user info for this endpoint
        return deliveryPackageService.getDeliveryPackageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/courier/active")
    public ResponseEntity<List<DeliveryPackageDto>> getCourierActiveDeliveryPackages(
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        List<DeliveryPackageDto> packages = deliveryPackageService.getCourierActiveDeliveryPackages(username);
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/customer")
    public ResponseEntity<List<DeliveryPackageDto>> getCustomerDeliveryPackages(
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        List<DeliveryPackageDto> packages = deliveryPackageService.getCustomerDeliveryPackages(username);
        return ResponseEntity.ok(packages);
    }

    @PostMapping("/{id}/take")
    public ResponseEntity<DeliveryPackageDto> takeDeliveryPackage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        DeliveryPackageDto updatedPackage = deliveryPackageService.takeDeliveryPackage(id, username);
        return ResponseEntity.ok(updatedPackage);
    }

    @PostMapping("/{id}/drop")
    public ResponseEntity<DeliveryPackageDto> dropDeliveryPackage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        DeliveryPackageDto updatedPackage = deliveryPackageService.dropDeliveryPackage(id, username);
        return ResponseEntity.ok(updatedPackage);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryPackageDto> updateDeliveryStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        DeliveryPackage.DeliveryStatus status = DeliveryPackage.DeliveryStatus.valueOf(request.get("status"));
        DeliveryPackageDto updatedPackage = deliveryPackageService.updateDeliveryStatus(id, username, status);
        return ResponseEntity.ok(updatedPackage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeliveryPackage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        jwtUtils.validateToken(jwtToken); // Validate token but we don't need the user info for this endpoint
        deliveryPackageService.deleteDeliveryPackage(id);
        return ResponseEntity.ok().build();
    }
}
