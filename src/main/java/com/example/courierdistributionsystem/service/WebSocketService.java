/*
package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Package;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.HashMap;

@Service
public class WebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyNewPackageAvailable(Package package_) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_PACKAGE");
        message.put("package", package_);
        messagingTemplate.convertAndSend("/topic/packages/new", message);
    }

    public void notifyPackageStatusUpdate(Package package_) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "STATUS_UPDATE");
        message.put("package", package_);
        

        if (package_.getCourier() != null) {
            messagingTemplate.convertAndSend("/topic/courier/" + package_.getCourier().getId() + "/packages", message);
        }
        

        messagingTemplate.convertAndSend("/topic/customer/" + package_.getCustomer().getId() + "/packages", message);
    }

    public void notifyCourierAssignment(Long courierId, Object payload) {
        messagingTemplate.convertAndSend("/topic/courier/" + courierId, payload);
    }

    public void notifyCustomerUpdate(Long customerId, Object payload) {
        messagingTemplate.convertAndSend("/topic/customer/" + customerId, payload);
    }

    public void broadcastStatusUpdate(Object payload) {
        messagingTemplate.convertAndSend("/topic/status", payload);
    }
} */
