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
}
