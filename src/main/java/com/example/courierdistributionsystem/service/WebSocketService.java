package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Package;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class WebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyNewPackageAvailable(Package package_) {
        messagingTemplate.convertAndSend("/topic/packages/new", package_);
    }

    public void notifyPackageStatusUpdate(Package package_) {
        messagingTemplate.convertAndSend("/topic/packages/status/" + package_.getId(), package_);
        if (package_.getCourier() != null) {
            messagingTemplate.convertAndSend("/topic/couriers/" + package_.getCourier().getId() + "/packages", package_);
        }
        messagingTemplate.convertAndSend("/topic/customers/" + package_.getCustomer().getId() + "/packages", package_);
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
}
