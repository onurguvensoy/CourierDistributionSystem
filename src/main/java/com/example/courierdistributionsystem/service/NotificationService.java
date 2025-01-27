package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Notification;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.NotificationRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketService webSocketService;

    public Page<Notification> getUserNotifications(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public List<Notification> getUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    public void markAsRead(Long notificationId, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUser().equals(user)) {
            throw new IllegalArgumentException("Notification does not belong to this user");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    public void createPackageStatusNotification(DeliveryPackage DeliveryPackage, String message) {

        Notification customerNotification = new Notification();
        customerNotification.setUser(DeliveryPackage.getCustomer());
        customerNotification.setDeliveryPackage(DeliveryPackage);
        customerNotification.setType(Notification.NotificationType.STATUS_CHANGE);
        customerNotification.setMessage(message);
        customerNotification.setActionUrl("/customer/packages/" + DeliveryPackage.getId());
        notificationRepository.save(customerNotification);
        webSocketService.notifyCustomerUpdate(DeliveryPackage.getCustomer().getId(), createNotificationPayload(customerNotification));

    
        if (DeliveryPackage.getCourier() != null) {
            Notification courierNotification = new Notification();
            courierNotification.setUser(DeliveryPackage.getCourier());
            courierNotification.setDeliveryPackage(DeliveryPackage);
            courierNotification.setType(Notification.NotificationType.STATUS_CHANGE);
            courierNotification.setMessage(message);
            courierNotification.setActionUrl("/courier/packages/" + DeliveryPackage.getId());
            notificationRepository.save(courierNotification);
            webSocketService.notifyCourierAssignment(DeliveryPackage.getCourier().getId(), createNotificationPayload(courierNotification));
        }
    }

    public void createDeliveryAlert(DeliveryPackage deliveryPackage, String message) {
        Notification notification = new Notification();
        notification.setUser(deliveryPackage.getCustomer());
        notification.setDeliveryPackage(deliveryPackage);
        notification.setType(Notification.NotificationType.DELIVERY_ALERT);
        notification.setMessage(message);
        notification.setActionUrl("/customer/packages/" + deliveryPackage.getId());
        notificationRepository.save(notification);
        webSocketService.notifyCustomerUpdate(deliveryPackage.getCustomer().getId(), createNotificationPayload(notification));
    }

    public void createRatingRequest(DeliveryPackage deliveryPackage) {
        // For customer to rate courier
        Notification customerNotification = new Notification();
        customerNotification.setUser(deliveryPackage.getCustomer());
        customerNotification.setDeliveryPackage(deliveryPackage);
        customerNotification.setType(Notification.NotificationType.RATING_REQUEST);
        customerNotification.setMessage("Please rate your delivery experience with " + deliveryPackage.getCourier().getUsername());
        customerNotification.setActionUrl("/customer/packages/" + deliveryPackage.getId() + "/rate");
        notificationRepository.save(customerNotification);
        webSocketService.notifyCustomerUpdate(deliveryPackage.getCustomer().getId(), createNotificationPayload(customerNotification));
    }

    public void createSystemNotification(User user, String message, String actionUrl) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(Notification.NotificationType.SYSTEM_MESSAGE);
        notification.setMessage(message);
        notification.setActionUrl(actionUrl);
        notificationRepository.save(notification);
        webSocketService.notifyCustomerUpdate(user.getId(), createNotificationPayload(notification));
    }

    private Map<String, Object> createNotificationPayload(Notification notification) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", notification.getId());
        payload.put("type", notification.getType());
        payload.put("message", notification.getMessage());
        payload.put("actionUrl", notification.getActionUrl());
        payload.put("createdAt", notification.getCreatedAt());
        payload.put("read", notification.isRead());
        return payload;
    }
} 