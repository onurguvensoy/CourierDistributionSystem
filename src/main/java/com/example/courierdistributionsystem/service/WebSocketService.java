package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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
        message.put("packageId", deliveryPackage.getId());
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
    }
}
