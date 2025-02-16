package com.example.courierdistributionsystem.controller.SocketController;

import com.example.courierdistributionsystem.controller.restController.DeliveryPackageController;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.service.CourierService;
import com.example.courierdistributionsystem.service.CustomerService;
import com.example.courierdistributionsystem.service.DeliveryPackageService;
import com.example.courierdistributionsystem.socket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
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
    private DeliveryPackageController deliveryPackageController;
    @Autowired
    private DeliveryPackageService deliveryPackageService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CourierService courierService;

    @MessageMapping("/packages/available")
    public void getAvailablePackages(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = getUsername(headerAccessor);
            List<DeliveryPackage> packages = deliveryPackageService.getAvailableDeliveryPackages();
            
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
            logger.error("Failed to get available packages: {}", e.getMessage());
            sendErrorMessage(headerAccessor, "Failed to get available packages");
        }
    }

    @MessageMapping("/packages/active")
    public void getActivePackages(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = getUsername(headerAccessor);
            List<DeliveryPackage> packages = deliveryPackageService.getCourierActiveDeliveryPackages(username);

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
            logger.error("Failed to get active packages: {}", e.getMessage());
            sendErrorMessage(headerAccessor, "Failed to get active packages");
        }
    }

    @MessageMapping("/package/take")
    public void takeDelivery(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        if (user == null) {
            logger.error("No authenticated user found");
            return;
        }

        String username = user.getName();
        Long packageId = Long.valueOf(payload.get("packageId").toString());
        logger.debug("Received take delivery request from user: {} for package: {}", username, packageId);
        
        webSocketService.takeDelivery(username, packageId);
    }

    @MessageMapping("/package/drop")
    public void dropDelivery(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        if (user == null) {
            logger.error("No authenticated user found");
            return;
        }

        String username = user.getName();
        Long packageId = Long.valueOf(payload.get("packageId").toString());
        logger.debug("Received drop delivery request from user: {} for package: {}", username, packageId);
        
        webSocketService.dropDelivery(username, packageId);
    }

    @MessageMapping("/package/status/update")
    public void updatePackageStatus(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        if (user == null) {
            logger.error("No authenticated user found");
            return;
        }

        String username = user.getName();
        Long packageId = Long.valueOf(payload.get("packageId").toString());
        String status = payload.get("status").toString();
        logger.debug("Received status update request from user: {} for package: {} to status: {}", username, packageId, status);
        
        try {
            DeliveryPackage.DeliveryStatus newStatus = DeliveryPackage.DeliveryStatus.valueOf(status);
            webSocketService.updatePackageStatus(username, packageId, newStatus);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid status value: {}", status);
            webSocketService.sendErrorToUser(username, "Invalid status value: " + status);
        }
    }

    private Map<String, Object> convertToDTO(DeliveryPackage pkg) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("package_id", pkg.getPackage_id().longValue());
        dto.put("trackingNumber", pkg.getTrackingNumber());
        dto.put("customerUsername", pkg.getCustomerUsername());
        dto.put("courierUsername", pkg.getCourierUsername());
        dto.put("pickupAddress", pkg.getPickupAddress());
        dto.put("deliveryAddress", pkg.getDeliveryAddress());
        dto.put("weight", pkg.getWeight());
        dto.put("description", pkg.getDescription());
        dto.put("specialInstructions", pkg.getSpecialInstructions());
        dto.put("status", pkg.getStatus().name());
        dto.put("createdAt", pkg.getCreatedAt().toString());
        dto.put("updatedAt", pkg.getUpdatedAt() != null ? pkg.getUpdatedAt().toString() : null);
        
        if (pkg.getCustomerDetails() != null) {
            dto.put("customerDetails", pkg.getCustomerDetails());
        }
        
        if (pkg.getCourierDetails() != null) {
            dto.put("courierDetails", pkg.getCourierDetails());
        }
        
        return dto;
    }

    private void sendErrorMessage(SimpMessageHeaderAccessor headerAccessor, String message) {
        try {
            String username = getUsername(headerAccessor);
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/errors",
                Map.of("error", message)
            );
        } catch (Exception e) {
            logger.error("Failed to send error message: {}", e.getMessage());
        }
    }

    private String getUsername(SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        if (user == null) {
            throw new RuntimeException("No authenticated user found");
        }
        return user.getName();
    }
} 