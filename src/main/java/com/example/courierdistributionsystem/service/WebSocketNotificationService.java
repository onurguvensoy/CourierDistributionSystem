package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebSocketNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Package creation notification
    public void notifyNewPackage(DeliveryPackage deliveryPackage) {
        messagingTemplate.convertAndSend("/topic/packages/new", deliveryPackage);
        messagingTemplate.convertAndSend("/topic/packages/available", deliveryPackage);
        logger.info("Notified about new package: {}", deliveryPackage.getPackage_id());
    }

    // Package tracking updates
    public void sendTrackingUpdate(String username, Map<String, Object> trackingInfo) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/package/tracking",
            trackingInfo
        );
        logger.info("Sent tracking update to user: {}", username);
    }

    // Package status updates
    public void notifyPackageStatusUpdate(DeliveryPackage updatedPackage) {
        // Notify customer
        messagingTemplate.convertAndSendToUser(
            updatedPackage.getCustomer().getUsername(),
            "/queue/package/status",
            updatedPackage
        );

        // Notify courier if assigned
        if (updatedPackage.getCourier() != null) {
            messagingTemplate.convertAndSendToUser(
                updatedPackage.getCourier().getUsername(),
                "/queue/package/status",
                updatedPackage
            );
        }

        // Notify admin about status change
        messagingTemplate.convertAndSend("/topic/packages/status", updatedPackage);
        logger.info("Notified about package status update: {}", updatedPackage.getPackage_id());
    }

    // Package location updates
    public void notifyPackageLocationUpdate(DeliveryPackage updatedPackage) {
        messagingTemplate.convertAndSendToUser(
            updatedPackage.getCustomer().getUsername(),
            "/queue/package/location",
            updatedPackage
        );
        logger.info("Notified about package location update: {}", updatedPackage.getPackage_id());
    }

    // Package assignment notification
    public void notifyPackageAssignment(DeliveryPackage assignedPackage) {
        messagingTemplate.convertAndSendToUser(
            assignedPackage.getCustomer().getUsername(),
            "/queue/package/assigned",
            assignedPackage
        );

        messagingTemplate.convertAndSendToUser(
            assignedPackage.getCourier().getUsername(),
            "/queue/package/assigned",
            assignedPackage
        );

        // Broadcast available packages update
        messagingTemplate.convertAndSend("/topic/packages/available", Map.of(
            "type", "PACKAGE_ASSIGNED",
            "packageId", assignedPackage.getPackage_id()
        ));
        logger.info("Notified about package assignment: {}", assignedPackage.getPackage_id());
    }

    // Package drop notification
    public void notifyPackageDrop(DeliveryPackage droppedPackage) {
        messagingTemplate.convertAndSendToUser(
            droppedPackage.getCustomer().getUsername(),
            "/queue/package/dropped",
            droppedPackage
        );

        messagingTemplate.convertAndSendToUser(
            droppedPackage.getCourier().getUsername(),
            "/queue/package/dropped",
            droppedPackage
        );

        // Broadcast available packages update
        messagingTemplate.convertAndSend("/topic/packages/available", Map.of(
            "type", "PACKAGE_DROPPED",
            "packageId", droppedPackage.getPackage_id()
        ));
        logger.info("Notified about package drop: {}", droppedPackage.getPackage_id());
    }

    // Send available packages to courier
    public void sendAvailablePackages(String username, List<DeliveryPackage> packages) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/packages/available",
            packages
        );
        logger.info("Sent available packages to courier: {}", username);
    }

    // Send active packages to courier
    public void sendActivePackages(String username, List<DeliveryPackage> packages) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/packages/active",
            packages
        );
        logger.info("Sent active packages to courier: {}", username);
    }

    // Error message handling
    public void sendErrorMessage(String username, String errorMessage) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/errors",
            Map.of("error", errorMessage)
        );
        logger.error("Sent error message to user {}: {}", username, errorMessage);
    }

    // General notification methods
    public void sendToUser(String userId, String destination, Object payload) {
        try {
            String userDestination = "/user/" + userId + "/" + destination;
            logger.debug("Sending WebSocket message to {}: {}", userDestination, payload);
            messagingTemplate.convertAndSend(userDestination, payload);
        } catch (Exception e) {
            logger.error("Failed to send WebSocket message to user {}: {}", userId, e.getMessage(), e);
        }
    }

    public void sendNotification(String userId, Notification notification) {
        sendToUser(userId, "notification", notification);
    }

    public void broadcastMessage(String destination, Object payload) {
        try {
            logger.debug("Broadcasting WebSocket message to {}: {}", destination, payload);
            messagingTemplate.convertAndSend("/topic/" + destination, payload);
        } catch (Exception e) {
            logger.error("Failed to broadcast WebSocket message: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify about new delivery available
     */
    public void notifyNewDeliveryAvailable(DeliveryPackage deliveryPackage) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_PACKAGE");
        message.put("package", deliveryPackage);
        
        messagingTemplate.convertAndSend("/topic/packages", message);
        
        // Send to customer's specific topic
        messagingTemplate.convertAndSend(
            "/topic/customer/" + deliveryPackage.getCustomer().getUsername() + "/package-updates", 
            message
        );
        logger.info("Notified about new delivery available: {}", deliveryPackage.getPackage_id());
    }

    /**
     * Notify about delivery status update
     */
    public void notifyDeliveryStatusUpdate(DeliveryPackage deliveryPackage) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "STATUS_UPDATE");
        message.put("package", deliveryPackage);
        
        messagingTemplate.convertAndSend("/topic/packages", message);
        
        if (deliveryPackage.getCourier() != null) {
            messagingTemplate.convertAndSend(
                "/topic/courier/" + deliveryPackage.getCourier().getUsername() + "/packages",
                message
            );
        }
        
        messagingTemplate.convertAndSend(
            "/topic/customer/" + deliveryPackage.getCustomer().getUsername() + "/package-updates", 
            message
        );
        logger.info("Notified about delivery status update: {}", deliveryPackage.getPackage_id());
    }

    /**
     * Send package update to customer
     */
    public void sendPackageUpdate(String username, DeliveryPackage deliveryPackage) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "PACKAGE_UPDATE");
        message.put("packageId", deliveryPackage.getPackage_id());
        message.put("status", deliveryPackage.getStatus());
        message.put("currentLocation", deliveryPackage.getCurrentLocation());
        
        if (deliveryPackage.getCourier() != null) {
            message.put("courierName", deliveryPackage.getCourier().getUsername());
            message.put("courierPhone", deliveryPackage.getCourier().getPhoneNumber());
        }

        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/package-updates",
            message
        );
        logger.info("Sent package update to customer {}: {}", username, deliveryPackage.getPackage_id());
    }

    /**
     * Send location update to customer
     */
    public void sendLocationUpdate(String username, Long packageId, String location, Double latitude, Double longitude) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOCATION_UPDATE");
        message.put("packageId", packageId);
        message.put("location", location);
        message.put("latitude", latitude);
        message.put("longitude", longitude);

        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/location-updates",
            message
        );
        logger.info("Sent location update to customer {} for package {}", username, packageId);
    }

    /**
     * Send rating prompt to customer
     */
    public void sendRatingPrompt(String username, Long packageId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "RATING_PROMPT");
        message.put("packageId", packageId);
        message.put("message", "Please rate your delivery experience");

        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/rating-prompts",
            message
        );
        logger.info("Sent rating prompt to customer {} for package {}", username, packageId);
    }
} 