package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.Notification;
import com.example.courierdistributionsystem.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
            Pageable pageable,
            HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            logger.warn("No user found in session");
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        Map<String, Object> response = new HashMap<>();
        try {
            if (unreadOnly) {
                List<Notification> notifications = notificationService.getUnreadNotifications(username);
                response.put("notifications", notifications);
            } else {
                Page<Notification> notifications = notificationService.getUserNotifications(username, pageable);
                response.put("notifications", notifications.getContent());
                response.put("totalPages", notifications.getTotalPages());
                response.put("totalElements", notifications.getTotalElements());
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to get notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting notifications: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get notifications"));
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            logger.warn("No user found in session");
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        try {
            notificationService.markAsRead(id, username);
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to mark notification as read: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to mark notification as read"));
        }
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            logger.warn("No user found in session");
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        try {
            notificationService.markAllAsRead(username);
            return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to mark all notifications as read: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error marking all notifications as read: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to mark all notifications as read"));
        }
    }
} 