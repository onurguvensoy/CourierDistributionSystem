package com.example.courierdistributionsystem.socket;

import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.repository.jpa.CourierRepository;
import com.example.courierdistributionsystem.service.CourierService;
import com.example.courierdistributionsystem.service.DeliveryPackageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final DeliveryPackageService deliveryPackageService;

    public WebSocketService(
            SimpMessagingTemplate messagingTemplate,
            DeliveryPackageService deliveryPackageService,
            CourierService courierService, 
            CourierRepository courierRepository) {
        this.messagingTemplate = messagingTemplate;
        this.deliveryPackageService = deliveryPackageService;
  
    }

    @EventListener
    public void handleCourierEvent(Map<String, Object> event) {
        if (event == null) {
            logger.warn("Received null event");
            return;
        }

        String type = (String) event.get("type");
        if (type == null) {
            logger.warn("Event type is null");
            return;
        }

        switch (type) {
            case "COURIER_UPDATE":
                handleCourierUpdate(event);
                break;
            case "COURIER_LOCATION_UPDATE":
                handleCourierLocationUpdate(event);
                break;
            case "COURIER_CREATED":
                handleCourierCreated(event);
                break;
            default:
                logger.warn("Unknown event type: {}", type);
        }
    }

    private void handleCourierUpdate(Map<String, Object> event) {
        try {
            Long courierId = (Long) event.get("courierId");
            if (courierId == null) {
                logger.warn("Courier ID is null in update event");
                return;
            }

            Object courierObj = event.get("courier");
            if (!(courierObj instanceof Courier)) {
                logger.warn("Invalid courier object in update event");
                return;
            }

            Courier courier = (Courier) courierObj;
            messagingTemplate.convertAndSend("/topic/courier/" + courierId + "/update", courier);

            // If availability changed, update available packages
            Boolean availabilityChanged = (Boolean) event.get("availabilityChanged");
            if (Boolean.TRUE.equals(availabilityChanged)) {
                List<DeliveryPackage> availablePackages = deliveryPackageService.getAvailableDeliveryPackages();
                messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
            }
        } catch (ClassCastException e) {
            logger.error("Error processing courier update event: {}", e.getMessage());
        }
    }

    private void handleCourierLocationUpdate(Map<String, Object> event) {
        try {
            Long courierId = (Long) event.get("courierId");
            if (courierId == null) {
                logger.warn("Courier ID is null in location update event");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> location = (Map<String, Object>) event.get("location");
            if (location == null) {
                logger.warn("Location is null in update event");
                return;
            }

            messagingTemplate.convertAndSend("/topic/courier/" + courierId + "/location", location);
        } catch (ClassCastException e) {
            logger.error("Error processing courier location update event: {}", e.getMessage());
        }
    }

    private void handleCourierCreated(Map<String, Object> event) {
        try {
            Object courierObj = event.get("courier");
            if (!(courierObj instanceof Courier)) {
                logger.warn("Invalid courier object in created event");
                return;
            }

            Courier courier = (Courier) courierObj;
            messagingTemplate.convertAndSend("/topic/couriers/new", courier);
        } catch (ClassCastException e) {
            logger.error("Error processing courier created event: {}", e.getMessage());
        }
    }

    @Transactional
    public void takeDelivery(String username, Long packageId) {
        if (username == null || packageId == null) {
            logger.error("Invalid parameters: username={}, packageId={}", username, packageId);
            sendErrorToUser(username, "Invalid parameters provided");
            return;
        }

        try {
            logger.debug("Processing take delivery request from courier {} for package {}", username, packageId);
            
            // Validate package exists before attempting to take it
            DeliveryPackage existingPackage = deliveryPackageService.getDeliveryPackageById(packageId);
            if (existingPackage == null) {
                sendErrorToUser(username, "Package not found with ID: " + packageId);
                return;
            }

            if (existingPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
                sendErrorToUser(username, "Package is not available for pickup");
                return;
            }
            
            DeliveryPackage updatedPackage = deliveryPackageService.takeDeliveryPackage(packageId, username);
            
            // Send success message to the courier
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/package/status",
                Map.of(
                    "status", "success",
                    "message", "Package assigned successfully",
                    "packageId", packageId,
                    "package", updatedPackage
                )
            );

            // Notify the customer
            messagingTemplate.convertAndSendToUser(
                updatedPackage.getCustomerUsername(),
                "/queue/package/status",
                Map.of(
                    "status", "assigned",
                    "message", "Your package has been assigned to a courier",
                    "packageId", packageId,
                    "courierUsername", username,
                    "package", updatedPackage
                )
            );

            // Update available packages list for all couriers
            List<DeliveryPackage> availablePackages = deliveryPackageService.getAvailableDeliveryPackages();
            messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);

            // Update active deliveries for the courier
            List<DeliveryPackage> activeDeliveries = deliveryPackageService.getCourierActiveDeliveryPackages(username);
            messagingTemplate.convertAndSendToUser(username, "/queue/packages/active", activeDeliveries);

        } catch (Exception e) {
            logger.error("Error processing take delivery request: {}", e.getMessage(), e);
            sendErrorToUser(username, "Failed to take delivery: " + e.getMessage());
        }
    }

    @Transactional
    public void dropDelivery(String username, Long packageId) {
        if (username == null || packageId == null) {
            logger.error("Invalid parameters: username={}, packageId={}", username, packageId);
            sendErrorToUser(username, "Invalid parameters provided");
            return;
        }

        try {
            logger.debug("Processing drop delivery request from courier {} for package {}", username, packageId);
            
            // Validate package exists before attempting to drop it
            DeliveryPackage existingPackage = deliveryPackageService.getDeliveryPackageById(packageId);
            if (existingPackage == null) {
                sendErrorToUser(username, "Package not found with ID: " + packageId);
                return;
            }

            if (existingPackage.getCourierUsername() == null || !existingPackage.getCourierUsername().equals(username)) {
                sendErrorToUser(username, "Package is not assigned to you");
                return;
            }
            
            DeliveryPackage updatedPackage = deliveryPackageService.dropDeliveryPackage(packageId, username);
            
            // Send success message to the courier
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/package/status",
                Map.of(
                    "status", "success",
                    "message", "Package dropped successfully",
                    "packageId", packageId,
                    "package", updatedPackage
                )
            );

            // Notify the customer
            messagingTemplate.convertAndSendToUser(
                updatedPackage.getCustomerUsername(),
                "/queue/package/status",
                Map.of(
                    "status", "pending",
                    "message", "Your package is waiting for a new courier",
                    "packageId", packageId,
                    "package", updatedPackage
                )
            );


            List<DeliveryPackage> availablePackages = deliveryPackageService.getAvailableDeliveryPackages();
            messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);

            // Update active deliveries for the courier
            List<DeliveryPackage> activeDeliveries = deliveryPackageService.getCourierActiveDeliveryPackages(username);
            messagingTemplate.convertAndSendToUser(username, "/queue/packages/active", activeDeliveries);

        } catch (Exception e) {
            logger.error("Error processing drop delivery request: {}", e.getMessage(), e);
            sendErrorToUser(username, "Failed to drop delivery: " + e.getMessage());
        }
    }

    @Transactional
    public void updatePackageStatus(String username, Long packageId, DeliveryPackage.DeliveryStatus newStatus) {
        try {
            logger.debug("Processing status update request from courier {} for package {} to status {}", 
                username, packageId, newStatus);
            
            DeliveryPackage updatedPackage = deliveryPackageService.updateDeliveryStatus(packageId, username, newStatus);
            
            // Send success message to the courier
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/package/status",
                Map.of(
                    "status", "success",
                    "message", "Package status updated successfully",
                    "packageId", packageId,
                    "newStatus", newStatus
                )
            );

            // Notify the customer
            messagingTemplate.convertAndSendToUser(
                updatedPackage.getCustomerUsername(),
                "/queue/package/status",
                Map.of(
                    "status", newStatus,
                    "message", "Your package status has been updated to " + newStatus,
                    "packageId", packageId
                )
            );

            // If the package becomes available again, update the available packages list
            if (newStatus == DeliveryPackage.DeliveryStatus.PENDING) {
                List<DeliveryPackage> availablePackages = deliveryPackageService.getAvailableDeliveryPackages();
                messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
            }

            // Update active deliveries for the courier
            List<DeliveryPackage> activeDeliveries = deliveryPackageService.getCourierActiveDeliveryPackages(username);
            messagingTemplate.convertAndSendToUser(username, "/queue/packages/active", activeDeliveries);

        } catch (Exception e) {
            logger.error("Error processing status update request: {}", e.getMessage());
            sendErrorToUser(username, "Failed to update status: " + e.getMessage());
        }
    }

    public void sendErrorToUser(String username, String errorMessage) {
        if (username == null) {
            logger.error("Cannot send error message to null username: {}", errorMessage);
            return;
        }
        
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", errorMessage);
        
        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/errors", error);
            logger.debug("Sent error message to user {}: {}", username, errorMessage);
        } catch (Exception e) {
            logger.error("Failed to send error message to user {}: {}", username, e.getMessage(), e);
        }
    }

    public void notifyPackageUpdate(DeliveryPackage deliveryPackage) {
        // Notify customer
        messagingTemplate.convertAndSendToUser(
            deliveryPackage.getCustomerUsername(),
            "/queue/package/status",
            deliveryPackage
        );

        // Notify courier if assigned
        if (deliveryPackage.getCourierUsername() != null) {
            messagingTemplate.convertAndSendToUser(
                deliveryPackage.getCourierUsername(),
                "/queue/package/status",
                deliveryPackage
            );
        }

        // Broadcast if package is available
        if (deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.PENDING) {
            messagingTemplate.convertAndSend("/topic/packages/available", 
                deliveryPackageService.getAvailableDeliveryPackages());
        }
    }
}
