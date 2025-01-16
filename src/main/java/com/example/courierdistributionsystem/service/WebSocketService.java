package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyPackageStatusUpdate(Package pkg) {
        // Notify customer about package status update
        messagingTemplate.convertAndSend(
            "/topic/customer/" + pkg.getCustomer().getId() + "/package-updates",
            pkg
        );

        // If package is assigned to a courier, notify them as well
        if (pkg.getCourier() != null) {
            messagingTemplate.convertAndSend(
                "/topic/courier/" + pkg.getCourier().getId() + "/package-updates",
                pkg
            );
        }
    }

    public void notifyNewPackageAvailable(Package pkg) {
        // Notify all couriers about new available package
        messagingTemplate.convertAndSend("/topic/couriers/available-packages", pkg);
    }
} 