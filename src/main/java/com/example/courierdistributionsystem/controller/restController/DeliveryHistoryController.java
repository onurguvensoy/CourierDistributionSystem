package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.dto.DeliveryHistoryDto;
import com.example.courierdistributionsystem.service.IDeliveryHistoryService;
import com.example.courierdistributionsystem.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/delivery-history")
public class DeliveryHistoryController {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryHistoryController.class);

    private final IDeliveryHistoryService deliveryHistoryService;
    private final JwtUtils jwtUtils;

    @Autowired
    public DeliveryHistoryController(IDeliveryHistoryService deliveryHistoryService, JwtUtils jwtUtils) {
        this.deliveryHistoryService = deliveryHistoryService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/courier")
    public ResponseEntity<List<DeliveryHistoryDto>> getCourierDeliveryHistory(
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        List<DeliveryHistoryDto> history = deliveryHistoryService.getCourierDeliveryHistory(username);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/courier/date-range")
    public ResponseEntity<List<DeliveryHistoryDto>> getCourierDeliveryHistoryByDateRange(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        List<DeliveryHistoryDto> history = deliveryHistoryService
                .getCourierDeliveryHistoryByDateRange(username, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/courier/package/{packageId}")
    public ResponseEntity<DeliveryHistoryDto> createDeliveryHistory(
            @RequestHeader("Authorization") String token,
            @PathVariable Long packageId) {
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        DeliveryHistoryDto history = deliveryHistoryService.createDeliveryHistory(username, packageId);
        return ResponseEntity.ok(history);
    }

    @PutMapping("/{historyId}")
    public ResponseEntity<Void> updateDeliveryHistory(
            @RequestHeader("Authorization") String token,
            @PathVariable Long historyId,
            @RequestBody DeliveryHistoryDto updates) {
        String jwtToken = token.replace("Bearer ", "");
        jwtUtils.validateToken(jwtToken);
        deliveryHistoryService.updateDeliveryHistory(historyId, updates);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{historyId}")
    public ResponseEntity<Void> deleteDeliveryHistory(
            @RequestHeader("Authorization") String token,
            @PathVariable Long historyId) {
        String jwtToken = token.replace("Bearer ", "");
        jwtUtils.validateToken(jwtToken);
        deliveryHistoryService.deleteDeliveryHistory(historyId);
        return ResponseEntity.ok().build();
    }
} 