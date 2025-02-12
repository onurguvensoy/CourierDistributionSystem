package com.example.courierdistributionsystem.socket;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.service.DeliveryPackageService;

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


    public void notifyNewPackage(DeliveryPackage deliveryPackage) {
        messagingTemplate.convertAndSend("/topic/packages/new", deliveryPackage);
        messagingTemplate.convertAndSend("/topic/packages/available", deliveryPackage);
        logger.info("Notified about new package: {}", deliveryPackage.getPackage_id());
    }


    public void updatePackageStatus(String username, Long packageId, DeliveryPackage.DeliveryStatus newStatus) {
        try {
            DeliveryPackage updatedPackage = deliveryPackageService.updateDeliveryStatus(packageId, username, newStatus);
            
        
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/package/status",
                Map.of(
                    "packageId", packageId,
                    "status", newStatus,
                    "message", getStatusUpdateMessage(newStatus)
                )
            );

            // Notify the customer about the status update
            messagingTemplate.convertAndSendToUser(
                updatedPackage.getCustomer().getUsername(),
                "/queue/package/status",
                Map.of(
                    "packageId", packageId,
                    "status", newStatus,
                    "message", getStatusUpdateMessage(newStatus)
                )
            );

            if (newStatus == DeliveryPackage.DeliveryStatus.DELIVERED) {
                sendAvailablePackagesUpdate();
            }

            logger.info("Package {} status updated to {} by courier {}", packageId, newStatus, username);
        } catch (Exception e) {
            logger.error("Failed to update package status: {}", e.getMessage());
            sendErrorToUser(username, "Failed to update package status: " + e.getMessage());
        }
    }

    // Take delivery with enhanced error handling
    public void takeDelivery(String username, Long packageId) {
        try {
            DeliveryPackage takenPackage = deliveryPackageService.takeDeliveryPackage(packageId, username);
            
           
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/package/assigned",
                Map.of(
                    "packageId", packageId,
                    "trackingNumber", takenPackage.getTrackingNumber(),
                    "message", "Package successfully assigned"
                )
            );

       
            messagingTemplate.convertAndSendToUser(
                takenPackage.getCustomer().getUsername(),
                "/queue/package/status",
                Map.of(
                    "packageId", packageId,
                    "status", "ASSIGNED",
                    "courierUsername", username,
                    "message", "Your package has been assigned to a courier"
                )
            );

       
            sendAvailablePackagesUpdate();
            
            logger.info("Package {} taken by courier {}", packageId, username);
        } catch (Exception e) {
            logger.error("Failed to take package: {}", e.getMessage());
            sendErrorToUser(username, "Failed to take package: " + e.getMessage());
        }
    }

    // Drop delivery with enhanced error handling
    public void dropDelivery(String username, Long packageId) {
        try {
            DeliveryPackage droppedPackage = deliveryPackageService.dropDeliveryPackage(packageId, username);
            
            // Notify the courier
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/package/dropped",
                Map.of(
                    "packageId", packageId,
                    "trackingNumber", droppedPackage.getTrackingNumber(),
                    "message", "Package dropped successfully"
                )
            );

            // Notify the customer
            messagingTemplate.convertAndSendToUser(
                droppedPackage.getCustomer().getUsername(),
                "/queue/package/status",
                Map.of(
                    "packageId", packageId,
                    "status", "PENDING",
                    "message", "Your package has been dropped by the courier"
                )
            );

            // Update available packages list
            sendAvailablePackagesUpdate();
            
            logger.info("Package {} dropped by courier {}", packageId, username);
        } catch (Exception e) {
            logger.error("Failed to drop package: {}", e.getMessage());
            sendErrorToUser(username, "Failed to drop package: " + e.getMessage());
        }
    }

    // Send active deliveries to courier
    public void sendActiveDeliveries(String username) {
        try {
            List<DeliveryPackage> activeDeliveries = deliveryPackageService.getCourierActiveDeliveryPackages(username);
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/packages/active",
                activeDeliveries
            );
            logger.info("Sent active deliveries to courier: {}", username);
        } catch (Exception e) {
            logger.error("Failed to send active deliveries: {}", e.getMessage());
            sendErrorToUser(username, "Failed to fetch active deliveries");
        }
    }

    // Send available packages update to all couriers
    private void sendAvailablePackagesUpdate() {
        try {
            List<DeliveryPackage> availablePackages = deliveryPackageService.getAvailableDeliveryPackages();
            messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
        } catch (Exception e) {
            logger.error("Failed to send available packages update: {}", e.getMessage());
        }
    }

    // Helper method to get status update message
    private String getStatusUpdateMessage(DeliveryPackage.DeliveryStatus status) {
        return switch (status) {
            case DELIVERED -> "Package marked as delivered successfully";
            case PICKED_UP -> "Package marked as picked up";
            case IN_TRANSIT -> "Package is now in transit";
            case ASSIGNED -> "Package has been assigned";
            case PENDING -> "Package is pending pickup";
            case CANCELLED -> "Package has been cancelled";
            default -> "Package status updated to " + status;
        };
    }

    // Send error message to specific user
    private void sendErrorToUser(String username, String errorMessage) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/errors",
            Map.of("error", errorMessage)
        );
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
        // Send location update to customer
        messagingTemplate.convertAndSendToUser(
            updatedPackage.getCustomer().getUsername(),
            "/queue/package/location",
            updatedPackage
        );
        logger.info("Notified about package location update: {}", updatedPackage.getPackage_id());
    }

    // Package assignment notification
    public void notifyPackageAssignment(DeliveryPackage assignedPackage) {
        // Notify customer about courier assignment
        messagingTemplate.convertAndSendToUser(
            assignedPackage.getCustomer().getUsername(),
            "/queue/package/assigned",
            assignedPackage
        );

        // Notify courier about successful assignment
        messagingTemplate.convertAndSendToUser(
            assignedPackage.getCourier().getUsername(),
            "/queue/package/assigned",
            assignedPackage
        );

        // Update available packages list for all couriers
        List<DeliveryPackage> availablePackages = getUpdatedAvailablePackages();
        messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
        logger.info("Notified about package assignment: {}", assignedPackage.getPackage_id());
    }

    // Package drop notification
    public void notifyPackageDrop(DeliveryPackage droppedPackage) {
        // Notify customer about package drop
        messagingTemplate.convertAndSendToUser(
            droppedPackage.getCustomer().getUsername(),
            "/queue/package/dropped",
            droppedPackage
        );

        // Update courier's active deliveries
        messagingTemplate.convertAndSendToUser(
            droppedPackage.getCourier().getUsername(),
            "/queue/package/dropped",
            droppedPackage
        );

        // Update available packages for all couriers
        List<DeliveryPackage> availablePackages = getUpdatedAvailablePackages();
        messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
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

    // Helper method to get updated available packages
    private List<DeliveryPackage> getUpdatedAvailablePackages() {
        try {
            return deliveryPackageService.getAvailableDeliveryPackages();
        } catch (Exception e) {
            logger.error("Failed to get available packages: {}", e.getMessage());
            return List.of();
        }
    }

    public void notifyNewDeliveryAvailable(DeliveryPackage deliveryPackage) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_PACKAGE");
        message.put("package", deliveryPackage);
        
        messagingTemplate.convertAndSend("/topic/packages", message);
        
        // Send to customer's specific topic
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

    /**
     * Send package status update to customer
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
