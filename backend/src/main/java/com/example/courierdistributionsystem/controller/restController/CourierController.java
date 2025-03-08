package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.dto.CourierDto;
import com.example.courierdistributionsystem.dto.LocationUpdateDto;
import com.example.courierdistributionsystem.service.ICourierService;
import com.example.courierdistributionsystem.service.IDeliveryPackageService;
import com.example.courierdistributionsystem.utils.JwtUtils;
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
    private final JwtUtils jwtUtils;

    @Autowired
    public CourierController(ICourierService courierService, 
                           IDeliveryPackageService deliveryPackageService,
                           JwtUtils jwtUtils) {
        this.courierService = courierService;
        this.deliveryPackageService = deliveryPackageService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/available")
    public ResponseEntity<List<CourierDto>> getAvailableCouriers(
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        jwtUtils.validateToken(jwtToken);
        List<CourierDto> couriers = courierService.getAllAvailableCouriers();
        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/{username}")
    public ResponseEntity<CourierDto> getCourierByUsername(
            @RequestHeader("Authorization") String token,
            @PathVariable String username) {
        String jwtToken = token.replace("Bearer ", "");
        jwtUtils.validateToken(jwtToken);
        return courierService.getCourierByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/location")
    public ResponseEntity<CourierDto> updateCourierLocation(
            @RequestHeader("Authorization") String token,
            @RequestBody LocationUpdateDto location) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        CourierDto updatedCourier = courierService.updateCourierLocation(username, location);
        return ResponseEntity.ok(updatedCourier);
    }

    @PutMapping("/availability")
    public ResponseEntity<CourierDto> updateCourierAvailability(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Boolean> request) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        CourierDto updatedCourier = courierService.updateCourierAvailability(username, request.get("available"));
        return ResponseEntity.ok(updatedCourier);
    }

    @GetMapping("/zone/{zone}")
    public ResponseEntity<List<CourierDto>> getCouriersByZone(
            @RequestHeader("Authorization") String token,
            @PathVariable String zone) {
        String jwtToken = token.replace("Bearer ", "");
        jwtUtils.validateToken(jwtToken);
        List<CourierDto> couriers = courierService.getCouriersByZone(zone);
        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCourierStats(
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        Map<String, Object> stats = courierService.getCourierStats(username);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/profile")
    public ResponseEntity<CourierDto> updateCourierProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> updates) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        CourierDto updatedCourier = courierService.updateCourierProfile(username, updates);
        return ResponseEntity.ok(updatedCourier);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCourier(
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        courierService.deleteCourier(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CourierDto>> getAllCouriers(
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        jwtUtils.validateToken(jwtToken);
        List<CourierDto> couriers = courierService.getAllCouriers();
        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<CourierDto> getCourierById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        jwtUtils.validateToken(jwtToken);
        return courierService.getCourierById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CourierDto> createCourier(
            @RequestHeader("Authorization") String token,
            @RequestBody CourierDto courierDto) {
        String jwtToken = token.replace("Bearer ", "");
        jwtUtils.validateToken(jwtToken);
        CourierDto createdCourier = courierService.createCourier(courierDto);
        return ResponseEntity.ok(createdCourier);
    }

    @PutMapping("/status")
    public ResponseEntity<Void> updateCourierStatus(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        courierService.updateCourierStatus(username, request.get("status"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/delivery-history")
    public ResponseEntity<Map<String, Object>> getCourierDeliveryHistory(
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        Map<String, Object> history = courierService.getCourierDeliveryHistory(username);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getCourierPerformanceMetrics(
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        Map<String, Object> metrics = courierService.getCourierPerformanceMetrics(username);
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/deliveries/{deliveryId}/assign")
    public ResponseEntity<Void> assignDeliveryToCourier(
            @RequestHeader("Authorization") String token,
            @PathVariable Long deliveryId) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        courierService.assignDeliveryToCourier(deliveryId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deliveries/{deliveryId}/unassign")
    public ResponseEntity<Void> unassignDeliveryFromCourier(
            @RequestHeader("Authorization") String token,
            @PathVariable Long deliveryId) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        courierService.unassignDeliveryFromCourier(deliveryId, username);
        return ResponseEntity.ok().build();
    }
}
