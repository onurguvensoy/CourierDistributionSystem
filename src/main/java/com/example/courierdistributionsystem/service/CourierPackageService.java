package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourierPackageService {

    @Autowired
    private DeliveryPackageRepository packageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketService webSocketService;

    public List<DeliveryPackage> getAvailableDeliveryPackages() {
        return packageRepository.findByStatus(DeliveryPackage.DeliveryStatus.PENDING);
    }

    public List<DeliveryPackage> getCourierActiveDeliveryPackages(String username) {
        User courier = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));

        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalArgumentException("User is not a courier");
        }

        return packageRepository.findByCourierAndStatusIn(courier, 
            List.of(DeliveryPackage.DeliveryStatus.ASSIGNED, 
                   DeliveryPackage.DeliveryStatus.PICKED_UP, 
                   DeliveryPackage.DeliveryStatus.IN_TRANSIT));
    }

    public DeliveryPackage takeDeliveryPackage(Long packageId, String courierUsername) {
        User courier = userRepository.findByUsername(courierUsername)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));

        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalArgumentException("User is not a courier");
        }

        if (!courier.getCourier().isAvailable()) {
            throw new IllegalArgumentException("Courier is not available");
        }

        DeliveryPackage deliveryPackage = packageRepository.findById(packageId)
            .orElseThrow(() -> new IllegalArgumentException("Package not found"));

        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            throw new IllegalArgumentException("Package is not available for pickup");
        }

        deliveryPackage.setCourier(courier);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.ASSIGNED);
        deliveryPackage.setAssignedAt(LocalDateTime.now());

        courier.getCourier().setAvailable(false);
        userRepository.save(courier);

        DeliveryPackage updatedPackage = packageRepository.save(deliveryPackage);
        webSocketService.notifyDeliveryStatusUpdate(updatedPackage);
        return updatedPackage;
    }

    public DeliveryPackage updateDeliveryStatus(Long packageId, String courierUsername, DeliveryPackage.DeliveryStatus newStatus) {
        DeliveryPackage deliveryPackage = packageRepository.findById(packageId)
            .orElseThrow(() -> new IllegalArgumentException("Package not found"));
            
        User courier = userRepository.findByUsername(courierUsername)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));

        if (!deliveryPackage.getCourier().equals(courier)) {
            throw new IllegalArgumentException("Package is not assigned to this courier");
        }

        validateStatusTransition(deliveryPackage.getStatus(), newStatus);
        deliveryPackage.setStatus(newStatus);

        switch (newStatus) {
            case PENDING:
                throw new IllegalArgumentException("Cannot set status back to PENDING");
            case ASSIGNED:
                if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
                    throw new IllegalArgumentException("Can only assign PENDING packages");
                }
                deliveryPackage.setAssignedAt(LocalDateTime.now());
                break;
            case PICKED_UP:
                if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.ASSIGNED) {
                    throw new IllegalArgumentException("Can only pick up ASSIGNED packages");
                }
                deliveryPackage.setPickedUpAt(LocalDateTime.now());
                break;
            case IN_TRANSIT:
                if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.PICKED_UP) {
                    throw new IllegalArgumentException("Can only transit PICKED_UP packages");
                }
                break;
            case DELIVERED:
                if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.IN_TRANSIT) {
                    throw new IllegalArgumentException("Can only deliver IN_TRANSIT packages");
                }
                deliveryPackage.setDeliveredAt(LocalDateTime.now());
                courier.getCourier().setAvailable(true);
                userRepository.save(courier);
                break;
            case CANCELLED:
                if (deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED) {
                    throw new IllegalArgumentException("Cannot cancel DELIVERED packages");
                }
                deliveryPackage.setCancelledAt(LocalDateTime.now());
                courier.getCourier().setAvailable(true);
                userRepository.save(courier);
                break;
        }

        DeliveryPackage updatedPackage = packageRepository.save(deliveryPackage);
        webSocketService.notifyDeliveryStatusUpdate(updatedPackage);
        return updatedPackage;
    }

    public DeliveryPackage updateDeliveryLocation(Long packageId, String courierUsername, Double latitude, Double longitude, String location) {
        DeliveryPackage deliveryPackage = packageRepository.findById(packageId)
            .orElseThrow(() -> new IllegalArgumentException("Package not found"));
            
        User courier = userRepository.findByUsername(courierUsername)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));

        if (!deliveryPackage.getCourier().equals(courier)) {
            throw new IllegalArgumentException("Package is not assigned to this courier");
        }

        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.IN_TRANSIT) {
            throw new IllegalArgumentException("Package must be in transit to update location");
        }

        deliveryPackage.setCurrentLatitude(latitude);
        deliveryPackage.setCurrentLongitude(longitude);
        deliveryPackage.setCurrentLocation(location);

        DeliveryPackage updatedPackage = packageRepository.save(deliveryPackage);
        webSocketService.notifyDeliveryLocationUpdate(updatedPackage);
        return updatedPackage;
    }

    private void validateStatusTransition(DeliveryPackage.DeliveryStatus currentStatus, DeliveryPackage.DeliveryStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case PENDING -> newStatus == DeliveryPackage.DeliveryStatus.ASSIGNED || newStatus == DeliveryPackage.DeliveryStatus.CANCELLED;
            case ASSIGNED -> newStatus == DeliveryPackage.DeliveryStatus.PICKED_UP || newStatus == DeliveryPackage.DeliveryStatus.CANCELLED;
            case PICKED_UP -> newStatus == DeliveryPackage.DeliveryStatus.IN_TRANSIT || newStatus == DeliveryPackage.DeliveryStatus.CANCELLED;
            case IN_TRANSIT -> newStatus == DeliveryPackage.DeliveryStatus.DELIVERED || newStatus == DeliveryPackage.DeliveryStatus.CANCELLED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!isValid) {
            throw new IllegalArgumentException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
    }
}
