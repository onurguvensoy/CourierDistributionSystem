package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.dto.LocationUpdateDto;
import com.example.courierdistributionsystem.model.LocationHistory;
import com.example.courierdistributionsystem.service.IDeliveryPackageService;
import com.example.courierdistributionsystem.service.LocationHistoryService;
import com.example.courierdistributionsystem.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class WebSocketLocationController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IDeliveryPackageService deliveryPackageService;
    private final LocationHistoryService locationHistoryService;

    public WebSocketLocationController(
            SimpMessagingTemplate messagingTemplate,
            IDeliveryPackageService deliveryPackageService,
            LocationHistoryService locationHistoryService) {
        this.messagingTemplate = messagingTemplate;
        this.deliveryPackageService = deliveryPackageService;
        this.locationHistoryService = locationHistoryService;
    }

    @MessageMapping("/package/{trackingNumber}/location")
    public void handleLocationUpdate(@Payload LocationUpdateDto locationUpdate, 
                                   String trackingNumber,
                                   Authentication authentication) {
        String courierUsername = authentication.getName();
        log.debug("Received location update from courier {} for package {}: {}", 
                 courierUsername, trackingNumber, locationUpdate);
        
        try {

//            locationUpdate.setTrackingNumber(trackingNumber);
            

//            LocationHistory history = locationHistoryService.saveLocationUpdate(
//                locationUpdate, courierUsername);
            

            messagingTemplate.convertAndSend(
                "/topic/package/" + trackingNumber + "/location",
                "adkjsfhdsa"
            );
            messagingTemplate.convertAndSend(
                    "/app/package/" + trackingNumber + "/location",
                    "adkjsfhdsa"
            );
            

            messagingTemplate.convertAndSendToUser(
                courierUsername,
                "/queue/package/" + trackingNumber + "/location/status",
                Map.of("status", "success", "message", "Location update saved successfully")
            );
            
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found error for tracking number {}: {}", trackingNumber, e.getMessage());
            messagingTemplate.convertAndSendToUser(
                courierUsername,
                "/queue/package/" + trackingNumber + "/location/status",
                Map.of("status", "error", "message", e.getMessage())
            );
        } catch (IllegalStateException e) {
            log.error("Invalid state error for tracking number {}: {}", trackingNumber, e.getMessage());
            messagingTemplate.convertAndSendToUser(
                courierUsername,
                "/queue/package/" + trackingNumber + "/location/status",
                Map.of("status", "error", "message", e.getMessage())
            );
        } catch (Exception e) {
            log.error("Unexpected error processing location update for tracking number {}: {}", 
                     trackingNumber, e.getMessage(), e);
            messagingTemplate.convertAndSendToUser(
                courierUsername,
                "/queue/package/" + trackingNumber + "/location/status",
                Map.of("status", "error", "message", "An unexpected error occurred")
            );
        }
    }

    @MessageMapping("/package/{trackingNumber}/history")
    public void handleHistoryRequest(@Payload Map<String, String> payload,
                                   String trackingNumber,
                                   Authentication authentication) {
        String username = authentication.getName();
        log.debug("Received history request for tracking number {} from user {}", 
                 trackingNumber, username);
        
        try {
            List<LocationHistory> history = locationHistoryService.getLocationHistory(trackingNumber);
            
            // Send history to the requesting user
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/package/" + trackingNumber + "/history",
                history
            );
            
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found error for tracking number {}: {}", trackingNumber, e.getMessage());
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/package/" + trackingNumber + "/history/status",
                Map.of("status", "error", "message", e.getMessage())
            );
        } catch (Exception e) {
            log.error("Unexpected error retrieving history for tracking number {}: {}", 
                     trackingNumber, e.getMessage(), e);
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/package/" + trackingNumber + "/history/status",
                Map.of("status", "error", "message", "An unexpected error occurred")
            );
        }
    }
} 