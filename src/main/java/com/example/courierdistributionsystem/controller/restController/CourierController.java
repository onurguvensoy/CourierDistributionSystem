package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.dto.CourierDto;
import com.example.courierdistributionsystem.dto.LocationUpdateDto;
import com.example.courierdistributionsystem.service.ICourierService;
import com.example.courierdistributionsystem.service.IDeliveryPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/couriers")
public class CourierController {

    private final ICourierService courierService;
    private final IDeliveryPackageService deliveryPackageService;

    @Autowired
    public CourierController(ICourierService courierService, IDeliveryPackageService deliveryPackageService) {
        this.courierService = courierService;
        this.deliveryPackageService = deliveryPackageService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<CourierDto>> getAvailableCouriers() {
        List<CourierDto> couriers = courierService.getAllAvailableCouriers();
        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/{username}")
    public ResponseEntity<CourierDto> getCourierByUsername(@PathVariable String username) {
        return courierService.getCourierByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{username}/location")
    public ResponseEntity<CourierDto> updateCourierLocation(
            @PathVariable String username,
            @RequestBody LocationUpdateDto location) {
        CourierDto updatedCourier = courierService.updateCourierLocation(username, location);
        return ResponseEntity.ok(updatedCourier);
    }

    @PutMapping("/{username}/availability")
    public ResponseEntity<CourierDto> updateCourierAvailability(
            @PathVariable String username,
            @RequestParam boolean available) {
        CourierDto updatedCourier = courierService.updateCourierAvailability(username, available);
        return ResponseEntity.ok(updatedCourier);
    }

    @GetMapping("/zone/{zone}")
    public ResponseEntity<List<CourierDto>> getCouriersByZone(@PathVariable String zone) {
        List<CourierDto> couriers = courierService.getCouriersByZone(zone);
        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/{username}/stats")
    public ResponseEntity<Map<String, Object>> getCourierStats(@PathVariable String username) {
        Map<String, Object> stats = courierService.getCourierStats(username);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/{username}/profile")
    public ResponseEntity<CourierDto> updateCourierProfile(
            @PathVariable String username,
            @RequestBody Map<String, String> updates) {
        CourierDto updatedCourier = courierService.updateCourierProfile(username, updates);
        return ResponseEntity.ok(updatedCourier);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteCourier(@PathVariable String username) {
        courierService.deleteCourier(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CourierDto>> getAllCouriers() {
        List<CourierDto> couriers = courierService.getAllCouriers();
        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<CourierDto> getCourierById(@PathVariable Long id) {
        return courierService.getCourierById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CourierDto> createCourier(@RequestBody CourierDto courierDto) {
        CourierDto createdCourier = courierService.createCourier(courierDto);
        return ResponseEntity.ok(createdCourier);
    }

    @PutMapping("/{username}/status")
    public ResponseEntity<Void> updateCourierStatus(
            @PathVariable String username,
            @RequestParam String status) {
        courierService.updateCourierStatus(username, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/delivery-history")
    public ResponseEntity<Map<String, Object>> getCourierDeliveryHistory(@PathVariable String username) {
        Map<String, Object> history = courierService.getCourierDeliveryHistory(username);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{username}/performance")
    public ResponseEntity<Map<String, Object>> getCourierPerformanceMetrics(@PathVariable String username) {
        Map<String, Object> metrics = courierService.getCourierPerformanceMetrics(username);
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/{username}/deliveries/{deliveryId}/assign")
    public ResponseEntity<Void> assignDeliveryToCourier(
            @PathVariable String username,
            @PathVariable Long deliveryId) {
        courierService.assignDeliveryToCourier(deliveryId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{username}/deliveries/{deliveryId}/unassign")
    public ResponseEntity<Void> unassignDeliveryFromCourier(
            @PathVariable String username,
            @PathVariable Long deliveryId) {
        courierService.unassignDeliveryFromCourier(deliveryId, username);
        return ResponseEntity.ok().build();
    }
}
