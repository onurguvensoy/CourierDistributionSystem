package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WebSocketService webSocketService;

    public List<Notification> getUnreadNotifications(String username) {
        logger.debug("Getting unread notifications for user: {}", username);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new IllegalArgumentException("User not found");
                });
        return notificationRepository.findByUserAndReadFalseOrderByTimestampDesc(user);
    }

    public Page<Notification> getUserNotifications(String username, Pageable pageable) {
        logger.debug("Getting notifications for user: {}", username);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new IllegalArgumentException("User not found");
                });
        return notificationRepository.findByUserOrderByTimestampDesc(user, pageable);
    }

    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByTimestampDesc(user);
    }

    public List<Notification> getUnreadNotificationsForUser(User user) {
        return notificationRepository.findByUserAndReadFalseOrderByTimestampDesc(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, String username) {
        logger.debug("Marking notification {} as read for user: {}", notificationId, username);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new IllegalArgumentException("User not found");
                });

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.warn("Notification not found with ID: {}", notificationId);
                    return new IllegalArgumentException("Notification not found");
                });

        if (!notification.belongsToUser(user)) {
            logger.warn("Notification {} does not belong to user: {}", notificationId, username);
            throw new IllegalArgumentException("Notification does not belong to this user");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        logger.info("Notification {} marked as read for user: {}", notificationId, username);
    }

    @Transactional
    public void markAllAsRead(String username) {
        logger.debug("Marking all notifications as read for user: {}", username);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new IllegalArgumentException("User not found");
                });

        List<Notification> unreadNotifications = notificationRepository.findByUserAndReadFalseOrderByTimestampDesc(user);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        logger.info("All notifications marked as read for user: {}", username);
    }

    @Transactional
    public void createDeliveryStatusNotification(DeliveryPackage deliveryPackage, String status) {
        // Notify customer
        Notification customerNotification = Notification.builder()
                .user(deliveryPackage.getCustomer())
                .deliveryPackage(deliveryPackage)
                .type(Notification.NotificationType.STATUS_CHANGE)
                .message("Your delivery status has been updated to: " + status)
                .timestamp(LocalDateTime.now())
                .build();
        notificationRepository.save(customerNotification);
        webSocketService.sendNotification(deliveryPackage.getCustomer().getId().toString(), customerNotification);

        // Notify courier if assigned
        if (deliveryPackage.getCourier() != null) {
            Notification courierNotification = Notification.builder()
                    .user(deliveryPackage.getCourier())
                    .deliveryPackage(deliveryPackage)
                    .type(Notification.NotificationType.STATUS_CHANGE)
                    .message("Delivery status updated to: " + status)
                    .timestamp(LocalDateTime.now())
                    .build();
            notificationRepository.save(courierNotification);
            webSocketService.sendNotification(deliveryPackage.getCourier().getId().toString(), courierNotification);
        }
    }

    @Transactional
    public void createDeliveryAssignmentNotification(DeliveryPackage deliveryPackage) {
        // Notify customer
        Notification customerNotification = Notification.builder()
                .user(deliveryPackage.getCustomer())
                .deliveryPackage(deliveryPackage)
                .type(Notification.NotificationType.DELIVERY_ALERT)
                .message("A courier has been assigned to your delivery")
                .timestamp(LocalDateTime.now())
                .build();
        notificationRepository.save(customerNotification);
        webSocketService.sendNotification(deliveryPackage.getCustomer().getId().toString(), customerNotification);

        // Notify courier
        Notification courierNotification = Notification.builder()
                .user(deliveryPackage.getCourier())
                .deliveryPackage(deliveryPackage)
                .type(Notification.NotificationType.DELIVERY_ALERT)
                .message("New delivery assigned to you")
                .timestamp(LocalDateTime.now())
                .build();
        notificationRepository.save(courierNotification);
        webSocketService.sendNotification(deliveryPackage.getCourier().getId().toString(), courierNotification);
    }

    @Transactional
    public void createSystemNotification(String message, User user) {
        Notification notification = Notification.builder()
                .user(user)
                .type(Notification.NotificationType.SYSTEM_MESSAGE)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        webSocketService.sendNotification(user.getId().toString(), notification);
    }

    // private Map<String, Object> createNotificationPayload(Notification notification) {
    //     Map<String, Object> payload = new HashMap<>();
    //     payload.put("id", notification.getId());
    //     payload.put("type", notification.getType());
    //     payload.put("message", notification.getMessage());
    //     payload.put("actionUrl", notification.getActionUrl());
    //     payload.put("createdAt", notification.getCreatedAt());
    //     payload.put("read", notification.isRead());
    //     return payload;
    // }
} 