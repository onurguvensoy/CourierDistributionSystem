package com.example.courierdistributionsystem.socket;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.service.DeliveryPackageService;
import com.example.courierdistributionsystem.service.ViewService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private DeliveryPackageService deliveryPackageService;

    @Autowired
    private ViewService viewService;

    public void notifyNewPackage(DeliveryPackage deliveryPackage) {
        messagingTemplate.convertAndSend("/topic/packages/new", deliveryPackage);
        messagingTemplate.convertAndSend("/topic/packages/available", deliveryPackage);
        logger.info("Notified about new package: {}", deliveryPackage.getPackage_id());
    }

    public void updatePackageStatus(String username, Long packageId, DeliveryPackage.DeliveryStatus status) {
        try {
            viewService.updateDeliveryStatus(packageId, username, status.toString());
            sendSuccessToUser(username, "Successfully updated package #" + packageId + " status to " + status);
            updateActiveDeliveriesForUser(username);
        } catch (Exception e) {
            logger.error("Error updating status for user {} package {}: {}", username, packageId, e.getMessage());
            sendErrorToUser(username, e.getMessage());
        }
    }

    public void takeDelivery(String username, Long packageId) {
        try {
            viewService.takeDeliveryPackage(packageId, username);
            sendSuccessToUser(username, "Successfully took delivery of package #" + packageId);
            broadcastAvailablePackages();
            updateActiveDeliveriesForUser(username);
        } catch (Exception e) {
            logger.error("Error taking delivery for user {} package {}: {}", username, packageId, e.getMessage());
            sendErrorToUser(username, e.getMessage());
        }
    }

    public void dropDelivery(String username, Long packageId) {
        try {
            viewService.dropDeliveryPackage(packageId, username);
            sendSuccessToUser(username, "Successfully dropped package #" + packageId);
            broadcastAvailablePackages();
            updateActiveDeliveriesForUser(username);
        } catch (Exception e) {
            logger.error("Error dropping delivery for user {} package {}: {}", username, packageId, e.getMessage());
            sendErrorToUser(username, e.getMessage());
        }
    }

    public void sendErrorToUser(String username, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        messagingTemplate.convertAndSendToUser(username, "/queue/errors", error);
    }

    private void sendSuccessToUser(String username, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        messagingTemplate.convertAndSendToUser(username, "/queue/package/status", response);
    }

    private void broadcastAvailablePackages() {
        List<DeliveryPackage> availablePackages = viewService.getAvailablePackages();
        messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
    }

    private void updateActiveDeliveriesForUser(String username) {
        List<DeliveryPackage> activeDeliveries = viewService.getActiveDeliveries(viewService.getUserByUsername(username));
        messagingTemplate.convertAndSendToUser(username, "/queue/packages/active", activeDeliveries);
    }

    public void sendTrackingUpdate(String username, Map<String, Object> trackingInfo) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/package/tracking",
            trackingInfo
        );
        logger.info("Sent tracking update to user: {}", username);
    }

    public void notifyPackageStatusUpdate(DeliveryPackage updatedPackage) {
        messagingTemplate.convertAndSendToUser(
            updatedPackage.getCustomer().getUsername(),
            "/queue/package/status",
            updatedPackage
        );

        if (updatedPackage.getCourier() != null) {
            messagingTemplate.convertAndSendToUser(
                updatedPackage.getCourier().getUsername(),
                "/queue/package/status",
                updatedPackage
            );
        }

        messagingTemplate.convertAndSend("/topic/packages/status", updatedPackage);
        logger.info("Notified about package status update: {}", updatedPackage.getPackage_id());
    }

    public void notifyPackageLocationUpdate(DeliveryPackage updatedPackage) {
        messagingTemplate.convertAndSendToUser(
            updatedPackage.getCustomer().getUsername(),
            "/queue/package/location",
            updatedPackage
        );
        logger.info("Notified about package location update: {}", updatedPackage.getPackage_id());
    }

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

        List<DeliveryPackage> availablePackages = getUpdatedAvailablePackages();
        messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
        logger.info("Notified about package assignment: {}", assignedPackage.getPackage_id());
    }

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

        List<DeliveryPackage> availablePackages = getUpdatedAvailablePackages();
        messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
        logger.info("Notified about package drop: {}", droppedPackage.getPackage_id());
    }

    public void sendAvailablePackages(String username, List<DeliveryPackage> packages) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/packages/available",
            packages
        );
        logger.info("Sent available packages to courier: {}", username);
    }

    public void sendActivePackages(String username, List<DeliveryPackage> packages) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/packages/active",
            packages
        );
        logger.info("Sent active packages to courier: {}", username);
    }

    public void sendErrorMessage(String username, String errorMessage) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/errors",
            Map.of("error", errorMessage)
        );
        logger.error("Sent error message to user {}: {}", username, errorMessage);
    }

    public List<DeliveryPackage> getUpdatedAvailablePackages() {
        try {
            return deliveryPackageService.getAvailableDeliveryPackages();
        } catch (Exception e) {
            logger.error("Failed to get updated available packages: {}", e.getMessage());
            return List.of();
        }
    }

    public void notifyNewDeliveryAvailable(DeliveryPackage deliveryPackage) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_PACKAGE");
        message.put("package", deliveryPackage);
        
        messagingTemplate.convertAndSend("/topic/packages", message);
        
        messagingTemplate.convertAndSend("/topic/customer/" + deliveryPackage.getCustomer().getUsername() + "/package-updates", message);
    }

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
        
        messagingTemplate.convertAndSend("/topic/customer/" + deliveryPackage.getCustomer().getUsername() + "/package-updates", message);
    }

    public void notifyDeliveryLocationUpdate(DeliveryPackage deliveryPackage) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOCATION_UPDATE");
        message.put("package", deliveryPackage);
        
        messagingTemplate.convertAndSend("/topic/packages", message);
        
        if (deliveryPackage.getCourier() != null) {
            messagingTemplate.convertAndSend(
                "/topic/courier/" + deliveryPackage.getCourier().getUsername() + "/packages",
                message
            );
        }
    }

    public void notifyCustomerUpdate(Long userId, Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/customer/" + userId + "/package-updates", payload);
    }

    public void notifyCourierAssignment(Long userId, Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/courier/" + userId + "/packages", payload);
    }

    public void broadcastStatusUpdate(Object payload) {
        messagingTemplate.convertAndSend("/topic/status", payload);
    }

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
    }

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
    }

    public void sendToUser(String userId, String destination, Object payload) {
        try {
            String userDestination = "/user/" + userId + "/" + destination;
            logger.debug("Sending WebSocket message to {}: {}", userDestination, payload);
            messagingTemplate.convertAndSend(userDestination, payload);
        } catch (Exception e) {
            logger.error("Failed to send WebSocket message to user {}: {}", userId, e.getMessage(), e);
        }
    }
}
