package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CourierRepository courierRepository;


    public Page<Notification> getUserNotifications(String username, Pageable pageable) {
        Customer customer = customerRepository.findByUsername(username)
                .orElse(null);
        if (customer != null) {
            return notificationRepository.findByCustomerOrderByCreatedAtDesc(customer, pageable);
        }

        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findByCourierOrderByCreatedAtDesc(courier, pageable);
    }

    public List<Notification> getUnreadNotifications(String username) {
        Customer customer = customerRepository.findByUsername(username)
                .orElse(null);
        if (customer != null) {
            return notificationRepository.findByCustomerAndIsReadFalseOrderByCreatedAtDesc(customer);
        }

        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findByCourierAndIsReadFalseOrderByCreatedAtDesc(courier);
    }

    public void markAsRead(Long notificationId, String username) {
        Customer customer = customerRepository.findByUsername(username)
                .orElse(null);
        Courier courier = null;
        if (customer == null) {
            courier = courierRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        
        if ((customer != null && !notification.belongsToUser(customer)) ||
            (courier != null && !notification.belongsToUser(courier))) {
            throw new IllegalArgumentException("Notification does not belong to this user");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String username) {
        Customer customer = customerRepository.findByUsername(username)
                .orElse(null);
        if (customer != null) {
            List<Notification> unreadNotifications = notificationRepository.findByCustomerAndIsReadFalseOrderByCreatedAtDesc(customer);
            unreadNotifications.forEach(notification -> notification.setRead(true));
            notificationRepository.saveAll(unreadNotifications);
        } else {
            Courier courier = courierRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            List<Notification> unreadNotifications = notificationRepository.findByCourierAndIsReadFalseOrderByCreatedAtDesc(courier);
            unreadNotifications.forEach(notification -> notification.setRead(true));
            notificationRepository.saveAll(unreadNotifications);
        }
    }

    public void createDeliveryStatusNotification(DeliveryPackage deliveryPackage) {
        Notification customerNotification = new Notification();
        customerNotification.setUser(deliveryPackage.getCustomer());
        customerNotification.setDeliveryPackage(deliveryPackage);
        customerNotification.setType(Notification.NotificationType.STATUS_CHANGE);
        customerNotification.setMessage("Your delivery status has been updated to: " + deliveryPackage.getStatus());
        notificationRepository.save(customerNotification);

        if (deliveryPackage.getCourier() != null) {
            Notification courierNotification = new Notification();
            courierNotification.setUser(deliveryPackage.getCourier());
            courierNotification.setDeliveryPackage(deliveryPackage);
            courierNotification.setType(Notification.NotificationType.STATUS_CHANGE);
            courierNotification.setMessage("Delivery status updated to: " + deliveryPackage.getStatus());
            notificationRepository.save(courierNotification);
        }
    }

    public void createDeliveryAssignmentNotification(DeliveryPackage deliveryPackage) {
        Notification customerNotification = new Notification();
        customerNotification.setUser(deliveryPackage.getCustomer());
        customerNotification.setDeliveryPackage(deliveryPackage);
        customerNotification.setType(Notification.NotificationType.DELIVERY_ALERT);
        customerNotification.setMessage("A courier has been assigned to your delivery");
        notificationRepository.save(customerNotification);

        Notification courierNotification = new Notification();
        courierNotification.setUser(deliveryPackage.getCourier());
        courierNotification.setDeliveryPackage(deliveryPackage);
        courierNotification.setType(Notification.NotificationType.DELIVERY_ALERT);
        courierNotification.setMessage("New delivery package has been assigned to you");
        notificationRepository.save(courierNotification);
    }

    public void createSystemNotification(String username, String message) {
        Customer customer = customerRepository.findByUsername(username)
                .orElse(null);
        
        Notification notification = new Notification();
        if (customer != null) {
            notification.setUser(customer);
        } else {
            Courier courier = courierRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            notification.setUser(courier);
        }
        
        notification.setType(Notification.NotificationType.SYSTEM_MESSAGE);
        notification.setMessage(message);
        notificationRepository.save(notification);
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