package com.example.courierdistributionsystem.controller.SocketController;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.dto.DeliveryPackageDto;
import com.example.courierdistributionsystem.service.IDeliveryPackageService;
import com.example.courierdistributionsystem.service.ICustomerService;
import com.example.courierdistributionsystem.service.ICourierService;
import com.example.courierdistributionsystem.socket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private IDeliveryPackageService deliveryPackageService;

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private ICourierService courierService;

    @MessageMapping("/packages/available")
    public void getAvailablePackages(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = getUsername(headerAccessor);
            List<DeliveryPackageDto> packages = deliveryPackageService.getAvailableDeliveryPackages();
            
            // Convert to clean DTOs
            List<Map<String, Object>> packageDTOs = packages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/packages/available",
                packageDTOs
            );
        } catch (Exception e) {
            sendErrorMessage(headerAccessor, "Failed to fetch available packages: " + e.getMessage());
        }
    }

    @MessageMapping("/packages/active")
    public void getActivePackages(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = getUsername(headerAccessor);
            List<DeliveryPackageDto> packages = deliveryPackageService.getCourierActiveDeliveryPackages(username);

            // Convert to clean DTOs
            List<Map<String, Object>> packageDTOs = packages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/packages/active",
                packageDTOs
            );
        } catch (Exception e) {
            sendErrorMessage(headerAccessor, "Failed to fetch active packages: " + e.getMessage());
        }
    }

    @MessageMapping("/package/take")
    public void takeDelivery(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = getUsername(headerAccessor);
            Long packageId = Long.parseLong(payload.get("packageId").toString());
            webSocketService.takeDelivery(username, packageId);
        } catch (Exception e) {
            sendErrorMessage(headerAccessor, "Failed to take delivery: " + e.getMessage());
        }
    }

    @MessageMapping("/package/drop")
    public void dropDelivery(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = getUsername(headerAccessor);
            Long packageId = Long.parseLong(payload.get("packageId").toString());
            webSocketService.dropDelivery(username, packageId);
        } catch (Exception e) {
            sendErrorMessage(headerAccessor, "Failed to drop delivery: " + e.getMessage());
        }
    }

    @MessageMapping("/package/status/update")
    public void updatePackageStatus(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = getUsername(headerAccessor);
            Long packageId = Long.parseLong(payload.get("packageId").toString());
            String status = payload.get("status").toString();
            
            DeliveryPackage.DeliveryStatus newStatus = DeliveryPackage.DeliveryStatus.valueOf(status.toUpperCase());
            webSocketService.updatePackageStatus(username, packageId, newStatus);
        } catch (Exception e) {
            sendErrorMessage(headerAccessor, "Failed to update package status: " + e.getMessage());
        }
    }

    private Map<String, Object> convertToDTO(DeliveryPackageDto pkg) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", pkg.getId());
        dto.put("trackingNumber", pkg.getTrackingNumber());
        dto.put("customerUsername", pkg.getCustomerUsername());
        dto.put("courierUsername", pkg.getCourierUsername());
        dto.put("pickupAddress", pkg.getPickupAddress());
        dto.put("deliveryAddress", pkg.getDeliveryAddress());
        dto.put("weight", pkg.getWeight());
        dto.put("description", pkg.getDescription());
        dto.put("specialInstructions", pkg.getSpecialInstructions());
        dto.put("status", pkg.getStatus());
        dto.put("createdAt", pkg.getCreatedAt());
        dto.put("updatedAt", pkg.getUpdatedAt());
        dto.put("customerDetails", pkg.getCustomerDetails());
        dto.put("courierDetails", pkg.getCourierDetails());
        return dto;
    }

    private void sendErrorMessage(SimpMessageHeaderAccessor headerAccessor, String message) {
        String username = getUsername(headerAccessor);
        if (username != null) {
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/errors",
                Map.of("error", message)
            );
        }
    }

    private String getUsername(SimpMessageHeaderAccessor headerAccessor) {
        return (String) headerAccessor.getSessionAttributes().get("username");
    }
} 