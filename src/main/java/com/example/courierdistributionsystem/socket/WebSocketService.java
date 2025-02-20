package com.example.courierdistributionsystem.socket;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.dto.DeliveryPackageDto;
import com.example.courierdistributionsystem.service.IDeliveryPackageService;
import com.example.courierdistributionsystem.service.ICourierService;
import com.example.courierdistributionsystem.repository.jpa.CourierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final IDeliveryPackageService deliveryPackageService;

    public WebSocketService(
            SimpMessagingTemplate messagingTemplate,
            IDeliveryPackageService deliveryPackageService,
            ICourierService courierService, 
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

        try {
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
        } catch (Exception e) {
            logger.error("Error handling courier event: {}", e.getMessage(), e);
        }
    }

    private void handleCourierUpdate(Map<String, Object> event) {
        try {
            String username = (String) event.get("username");
            if (username == null) {
                logger.warn("Username is null in courier update event");
                return;
            }

            Boolean availabilityChanged = (Boolean) event.get("availabilityChanged");
            if (Boolean.TRUE.equals(availabilityChanged)) {
                List<DeliveryPackageDto> availablePackages = deliveryPackageService.getAvailableDeliveryPackages();
                messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
            }
        } catch (ClassCastException e) {
            logger.error("Invalid event data format: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error handling courier update: {}", e.getMessage(), e);
        }
    }

    private void handleCourierLocationUpdate(Map<String, Object> event) {
        try {
            String username = (String) event.get("username");
            if (username == null) {
                logger.warn("Username is null in location update event");
                return;
            }

            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", event.get("latitude"));
            locationData.put("longitude", event.get("longitude"));
            locationData.put("timestamp", event.get("timestamp"));

            messagingTemplate.convertAndSend(
                "/topic/couriers/" + username + "/location",
                locationData
            );
        } catch (ClassCastException e) {
            logger.error("Invalid location data format: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error handling location update: {}", e.getMessage(), e);
        }
    }

    private void handleCourierCreated(Map<String, Object> event) {
        try {
            String username = (String) event.get("username");
            if (username == null) {
                logger.warn("Username is null in courier created event");
                return;
            }

            messagingTemplate.convertAndSend(
                "/topic/couriers/created",
                Map.of("username", username)
            );
        } catch (Exception e) {
            logger.error("Error handling courier created event: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void takeDelivery(String username, Long packageId) {
        try {
            logger.info("Courier {} attempting to take package {}", username, packageId);
            
            // Validate package exists before attempting to take it
            Optional<DeliveryPackageDto> existingPackage = deliveryPackageService.getDeliveryPackageById(packageId);
            if (existingPackage.isEmpty()) {
                sendErrorToUser(username, "Package not found with ID: " + packageId);
                return;
            }

            if (existingPackage.get().getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
                sendErrorToUser(username, "Package is not available for pickup");
                return;
            }
            
            DeliveryPackageDto updatedPackage = deliveryPackageService.takeDeliveryPackage(packageId, username);
            
            // Send success message to the courier
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/packages/take/success",
                Map.of(
                    "message", "Successfully took package",
                    "package", updatedPackage
                )
            );

            // Notify the customer
            String customerUsername = updatedPackage.getCustomerUsername();
            if (customerUsername != null) {
                messagingTemplate.convertAndSendToUser(
                    customerUsername,
                    "/queue/packages/status",
                    Map.of(
                        "message", "Your package has been picked up by a courier",
                        "package", updatedPackage
                    )
                );
            }

            // Update available packages list for all couriers
            List<DeliveryPackageDto> availablePackages = deliveryPackageService.getAvailableDeliveryPackages();
            messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);

            // Update active deliveries for the courier
            List<DeliveryPackageDto> activeDeliveries = deliveryPackageService.getCourierActiveDeliveryPackages(username);
            messagingTemplate.convertAndSendToUser(username, "/queue/packages/active", activeDeliveries);

        } catch (Exception e) {
            logger.error("Error taking delivery: {}", e.getMessage(), e);
            sendErrorToUser(username, "Failed to take package: " + e.getMessage());
        }
    }

    @Transactional
    public void dropDelivery(String username, Long packageId) {
        try {
            logger.info("Courier {} attempting to drop package {}", username, packageId);
            
            // Validate package exists before attempting to drop it
            Optional<DeliveryPackageDto> existingPackage = deliveryPackageService.getDeliveryPackageById(packageId);
            if (existingPackage.isEmpty()) {
                sendErrorToUser(username, "Package not found with ID: " + packageId);
                return;
            }

            if (existingPackage.get().getStatus() != DeliveryPackage.DeliveryStatus.IN_PROGRESS) {
                sendErrorToUser(username, "Package is not in progress");
                return;
            }
            
            DeliveryPackageDto updatedPackage = deliveryPackageService.dropDeliveryPackage(packageId, username);
            
            // Send success message to the courier
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/packages/drop/success",
                Map.of(
                    "message", "Successfully dropped package",
                    "package", updatedPackage
                )
            );

            // Notify the customer
            String customerUsername = updatedPackage.getCustomerUsername();
            if (customerUsername != null) {
                messagingTemplate.convertAndSendToUser(
                    customerUsername,
                    "/queue/packages/status",
                    Map.of(
                        "message", "Your package has been delivered",
                        "package", updatedPackage
                    )
                );
            }

            // Update available packages list for all couriers
            List<DeliveryPackageDto> availablePackages = deliveryPackageService.getAvailableDeliveryPackages();
            messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);

            // Update active deliveries for the courier
            List<DeliveryPackageDto> activeDeliveries = deliveryPackageService.getCourierActiveDeliveryPackages(username);
            messagingTemplate.convertAndSendToUser(username, "/queue/packages/active", activeDeliveries);

        } catch (Exception e) {
            logger.error("Error dropping delivery: {}", e.getMessage(), e);
            sendErrorToUser(username, "Failed to drop package: " + e.getMessage());
        }
    }

    @Transactional
    public void updatePackageStatus(String username, Long packageId, DeliveryPackage.DeliveryStatus newStatus) {
        try {
            logger.info("Courier {} updating package {} status to {}", 
                username, packageId, newStatus);
            
            DeliveryPackageDto updatedPackage = deliveryPackageService.updateDeliveryStatus(packageId, username, newStatus);
            
            // Send success message to the courier
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/packages/status/success",
                Map.of(
                    "message", "Successfully updated package status",
                    "package", updatedPackage
                )
            );

            // Notify the customer
            String customerUsername = updatedPackage.getCustomerUsername();
            if (customerUsername != null) {
                messagingTemplate.convertAndSendToUser(
                    customerUsername,
                    "/queue/packages/status",
                    Map.of(
                        "message", "Your package status has been updated",
                        "package", updatedPackage
                    )
                );
            }

            // If the package becomes available again, update the available packages list
            if (newStatus == DeliveryPackage.DeliveryStatus.PENDING) {
                List<DeliveryPackageDto> availablePackages = deliveryPackageService.getAvailableDeliveryPackages();
                messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
            }

            // Update active deliveries for the courier
            List<DeliveryPackageDto> activeDeliveries = deliveryPackageService.getCourierActiveDeliveryPackages(username);
            messagingTemplate.convertAndSendToUser(username, "/queue/packages/active", activeDeliveries);

        } catch (Exception e) {
            logger.error("Error updating package status: {}", e.getMessage(), e);
            sendErrorToUser(username, "Failed to update package status: " + e.getMessage());
        }
    }

    public void sendErrorToUser(String username, String errorMessage) {
        try {
            logger.debug("Sending error message to user {}: {}", username, errorMessage);
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/errors",
                Map.of("error", errorMessage)
            );
        } catch (Exception e) {
            logger.error("Failed to send error message to user: {}", e.getMessage(), e);
        }
    }

    public void notifyPackageUpdate(DeliveryPackageDto deliveryPackage) {
        try {
            // Notify the customer
            String customerUsername = deliveryPackage.getCustomerUsername();
            if (customerUsername != null) {
                messagingTemplate.convertAndSendToUser(
                    customerUsername,
                    "/queue/packages/status",
                    Map.of(
                        "message", "Your package has been updated",
                        "package", deliveryPackage
                    )
                );
            }

            // Notify the courier if assigned
            String courierUsername = deliveryPackage.getCourierUsername();
            if (courierUsername != null) {
                messagingTemplate.convertAndSendToUser(
                    courierUsername,
                    "/queue/packages/status",
                    Map.of(
                        "message", "Package has been updated",
                        "package", deliveryPackage
                    )
                );
            }

            // Update available packages list if the package is pending
            if (deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.PENDING) {
                List<DeliveryPackageDto> availablePackages = deliveryPackageService.getAvailableDeliveryPackages();
                messagingTemplate.convertAndSend("/topic/packages/available", availablePackages);
            }
        } catch (Exception e) {
            logger.error("Error notifying package update: {}", e.getMessage(), e);
        }
    }
}
