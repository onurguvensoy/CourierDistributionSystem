package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class DeliveryPackageService {

    @Autowired
    private DeliveryPackageRepository deliveryPackageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketService webSocketService;

    public List<DeliveryPackage> getAllDeliveryPackages() {
        return deliveryPackageRepository.findAll();
    }

    public DeliveryPackage getDeliveryPackageById(Long id) {
        return deliveryPackageRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Delivery package not found with id: " + id));
    }

    public DeliveryPackage createDeliveryPackage(Map<String, String> deliveryRequest) {
        String username = deliveryRequest.get("username");
        String pickupAddress = deliveryRequest.get("pickupAddress");
        String deliveryAddress = deliveryRequest.get("deliveryAddress");
        String description = deliveryRequest.get("description");
        String weightStr = deliveryRequest.get("weight");
        String specialInstructions = deliveryRequest.get("specialInstructions");

        if (username == null || pickupAddress == null || deliveryAddress == null) {
            throw new IllegalArgumentException("Missing required fields");
        }

        User customer = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (customer.getRole() != User.UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Only customers can create delivery packages");
        }

        DeliveryPackage newDelivery = DeliveryPackage.builder()
            .customer(customer)
            .pickupAddress(pickupAddress)
            .deliveryAddress(deliveryAddress)
            .description(description)
            .specialInstructions(specialInstructions)
            .weight(weightStr != null ? Double.parseDouble(weightStr) : 0.0)
            .status(DeliveryPackage.DeliveryStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        DeliveryPackage savedDelivery = deliveryPackageRepository.save(newDelivery);
        webSocketService.notifyNewDeliveryAvailable(savedDelivery);
        return savedDelivery;
    }

    public DeliveryPackage updateDeliveryPackage(Long id, Map<String, String> deliveryRequest) {
        DeliveryPackage existingDelivery = getDeliveryPackageById(id);

        if (existingDelivery.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            throw new IllegalArgumentException("Can only update pending delivery packages");
        }

        if (deliveryRequest.containsKey("deliveryAddress")) {
            existingDelivery.setDeliveryAddress(deliveryRequest.get("deliveryAddress"));
        }
        if (deliveryRequest.containsKey("pickupAddress")) {
            existingDelivery.setPickupAddress(deliveryRequest.get("pickupAddress"));
        }
        if (deliveryRequest.containsKey("description")) {
            existingDelivery.setDescription(deliveryRequest.get("description"));
        }
        if (deliveryRequest.containsKey("weight")) {
            existingDelivery.setWeight(Double.parseDouble(deliveryRequest.get("weight")));
        }
        if (deliveryRequest.containsKey("specialInstructions")) {
            existingDelivery.setSpecialInstructions(deliveryRequest.get("specialInstructions"));
        }

        DeliveryPackage updatedDelivery = deliveryPackageRepository.save(existingDelivery);
        webSocketService.notifyDeliveryStatusUpdate(updatedDelivery);
        return updatedDelivery;
    }

    public void cancelDeliveryPackage(Long id, String username) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(id);
        User customer = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (!deliveryPackage.getCustomer().equals(customer)) {
            throw new IllegalArgumentException("Delivery package does not belong to this customer");
        }

        if (deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot cancel delivered packages");
        }

        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.CANCELLED);
        deliveryPackage.setCancelledAt(LocalDateTime.now());

        if (deliveryPackage.getCourier() != null) {
            deliveryPackage.getCourier().getCourier().setAvailable(true);
            userRepository.save(deliveryPackage.getCourier());
        }

        DeliveryPackage cancelledDelivery = deliveryPackageRepository.save(deliveryPackage);
        webSocketService.notifyDeliveryStatusUpdate(cancelledDelivery);
    }

    public List<DeliveryPackage> getCustomerDeliveryPackages(String username) {
        User customer = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        if (customer.getRole() != User.UserRole.CUSTOMER) {
            throw new IllegalArgumentException("User is not a customer");
        }
        
        return deliveryPackageRepository.findByCustomer(customer);
    }

    public Map<String, Object> trackDeliveryPackage(Long id, String username) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(id);
        User customer = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (!deliveryPackage.getCustomer().equals(customer)) {
            throw new IllegalArgumentException("Delivery package does not belong to this customer");
        }

        Map<String, Object> trackingInfo = new HashMap<>();
        trackingInfo.put("id", deliveryPackage.getId());
        trackingInfo.put("status", deliveryPackage.getStatus());
        trackingInfo.put("pickupAddress", deliveryPackage.getPickupAddress());
        trackingInfo.put("deliveryAddress", deliveryPackage.getDeliveryAddress());
        trackingInfo.put("currentLocation", deliveryPackage.getCurrentLocation());
        trackingInfo.put("currentLatitude", deliveryPackage.getCurrentLatitude());
        trackingInfo.put("currentLongitude", deliveryPackage.getCurrentLongitude());
        trackingInfo.put("createdAt", deliveryPackage.getCreatedAt());
        trackingInfo.put("assignedAt", deliveryPackage.getAssignedAt());
        trackingInfo.put("pickedUpAt", deliveryPackage.getPickedUpAt());
        trackingInfo.put("deliveredAt", deliveryPackage.getDeliveredAt());
        
        if (deliveryPackage.getCourier() != null) {
            trackingInfo.put("courierUsername", deliveryPackage.getCourier().getUsername());
        }

        return trackingInfo;
    }

    public List<DeliveryPackage> getAvailableDeliveryPackages() {
        return deliveryPackageRepository.findByStatus(DeliveryPackage.DeliveryStatus.PENDING);
    }

    public List<DeliveryPackage> getCourierActiveDeliveryPackages(String username) {
        User courier = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));

        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalArgumentException("User is not a courier");
        }

        return deliveryPackageRepository.findByCourierAndStatusIn(courier, 
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

        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);

        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            throw new IllegalArgumentException("Package is not available for pickup");
        }

        deliveryPackage.setCourier(courier);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.ASSIGNED);
        deliveryPackage.setAssignedAt(LocalDateTime.now());

        courier.getCourier().setAvailable(false);
        userRepository.save(courier);

        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);
        webSocketService.notifyDeliveryStatusUpdate(updatedPackage);
        return updatedPackage;
    }

    public DeliveryPackage updateDeliveryStatus(Long packageId, String courierUsername, DeliveryPackage.DeliveryStatus newStatus) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
            
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

        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);
        webSocketService.notifyDeliveryStatusUpdate(updatedPackage);
        return updatedPackage;
    }

    public DeliveryPackage updateDeliveryLocation(Long packageId, String courierUsername, Double latitude, Double longitude, String location) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
            
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

        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);
        webSocketService.notifyDeliveryLocationUpdate(updatedPackage);
        return updatedPackage;
    }

    public DeliveryPackage dropDeliveryPackage(Long packageId, String courierUsername) {
        User courier = userRepository.findByUsername(courierUsername)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));

        if (courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalArgumentException("User is not a courier");
        }

        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);

        if (!deliveryPackage.getCourier().equals(courier)) {
            throw new IllegalArgumentException("Package is not assigned to this courier");
        }

        if (deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED || 
            deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot drop a delivered or cancelled package");
        }

        deliveryPackage.setCourier(null);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.PENDING);
        deliveryPackage.setAssignedAt(null);

        courier.getCourier().setAvailable(true);
        userRepository.save(courier);

        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);
        webSocketService.notifyDeliveryStatusUpdate(updatedPackage);
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
