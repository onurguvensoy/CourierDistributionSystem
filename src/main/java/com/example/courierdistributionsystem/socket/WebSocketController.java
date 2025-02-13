package com.example.courierdistributionsystem.socket;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.service.ViewService;
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

@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private ViewService viewService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/ws/packages/available")
    public void getAvailablePackages(SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        if (user == null) {
            logger.error("No authenticated user found");
            return;
        }

        String username = user.getName();
        logger.debug("Received request for available packages from user: {}", username);
        
        try {
            List<DeliveryPackage> availablePackages = viewService.getAvailablePackages();
            messagingTemplate.convertAndSendToUser(username, "/queue/packages/available", availablePackages);
        } catch (Exception e) {
            logger.error("Error getting available packages for user {}: {}", username, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get available packages");
            messagingTemplate.convertAndSendToUser(username, "/queue/errors", error);
        }
    }

    @MessageMapping("/ws/packages/active")
    public void getActiveDeliveries(SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        if (user == null) {
            logger.error("No authenticated user found");
            return;
        }

        String username = user.getName();
        logger.debug("Received request for active deliveries from user: {}", username);
        
        try {
            List<DeliveryPackage> activeDeliveries = viewService.getActiveDeliveries(viewService.getUserByUsername(username));
            messagingTemplate.convertAndSendToUser(username, "/queue/packages/active", activeDeliveries);
        } catch (Exception e) {
            logger.error("Error getting active deliveries for user {}: {}", username, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get active deliveries");
            messagingTemplate.convertAndSendToUser(username, "/queue/errors", error);
        }
    }

    @MessageMapping("/ws/package/take")
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

    @MessageMapping("/ws/package/drop")
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

    @MessageMapping("/ws/package/status/update")
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

    private void broadcastAvailablePackages() {
        List<DeliveryPackage> availablePackages = viewService.getAvailablePackages();
        messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
    }
} 