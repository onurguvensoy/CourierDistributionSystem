package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.Notification;
import com.example.courierdistributionsystem.service.NotificationService;
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
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getUserNotifications(@RequestParam String username, Pageable pageable) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<Notification> notifications = notificationService.getUserNotifications(username, pageable);
            response.put("status", "success");
            response.put("data", notifications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(username);
            response.put("status", "success");
            response.put("data", notifications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, @RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            notificationService.markAsRead(id, username);
            response.put("status", "success");
            response.put("message", "Notification marked as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            notificationService.markAllAsRead(username);
            response.put("status", "success");
            response.put("message", "All notifications marked as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 