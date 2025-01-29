package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.repository.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.CustomerRepository;
import com.example.courierdistributionsystem.repository.CourierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class PackageService {

    @Autowired
    private DeliveryPackageRepository packageRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private WebSocketService webSocketService;

    public List<DeliveryPackage> getAllPackages() {
        return packageRepository.findAll();
    }

    public DeliveryPackage getPackageById(Long id) {
        return packageRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Package not found with id: " + id));
    }

    public DeliveryPackage createPackage(Map<String, String> packageRequest) {
        String username = packageRequest.get("username");
        String pickupAddress = packageRequest.get("pickupAddress");
        String deliveryAddress = packageRequest.get("deliveryAddress");
        String description = packageRequest.get("description");
        String weightStr = packageRequest.get("weight");
        String specialInstructions = packageRequest.get("specialInstructions");

        if (username == null || pickupAddress == null || deliveryAddress == null) {
            throw new IllegalArgumentException("Missing required fields");
        }

        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        DeliveryPackage newPackage = DeliveryPackage.builder()
            .customer(customer)
            .pickupAddress(pickupAddress)
            .deliveryAddress(deliveryAddress)
            .description(description)
            .specialInstructions(specialInstructions)
            .weight(weightStr != null ? Double.parseDouble(weightStr) : 0.0)
            .status(DeliveryPackage.DeliveryStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        DeliveryPackage savedPackage = packageRepository.save(newPackage);
        webSocketService.notifyNewDeliveryAvailable(savedPackage);
        return savedPackage;
    }

    public DeliveryPackage updatePackage(Long id, Map<String, String> packageRequest) {
        DeliveryPackage existingPackage = getPackageById(id);

        if (existingPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            throw new IllegalArgumentException("Can only update pending packages");
        }

        if (packageRequest.containsKey("deliveryAddress")) {
            existingPackage.setDeliveryAddress(packageRequest.get("deliveryAddress"));
        }
        if (packageRequest.containsKey("pickupAddress")) {
            existingPackage.setPickupAddress(packageRequest.get("pickupAddress"));
        }
        if (packageRequest.containsKey("description")) {
            existingPackage.setDescription(packageRequest.get("description"));
        }
        if (packageRequest.containsKey("weight")) {
            existingPackage.setWeight(Double.parseDouble(packageRequest.get("weight")));
        }
        if (packageRequest.containsKey("specialInstructions")) {
            existingPackage.setSpecialInstructions(packageRequest.get("specialInstructions"));
        }

        DeliveryPackage updatedPackage = packageRepository.save(existingPackage);
        webSocketService.notifyDeliveryStatusUpdate(updatedPackage);
        return updatedPackage;
    }

    public void cancelPackage(Long id, String username) {
        DeliveryPackage deliveryPackage = getPackageById(id);
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (!deliveryPackage.getCustomer().equals(customer)) {
            throw new IllegalArgumentException("Package does not belong to this customer");
        }

        if (deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED || 
            deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot cancel package in current status");
        }

        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.CANCELLED);
        deliveryPackage.setCancelledAt(LocalDateTime.now());

        if (deliveryPackage.getCourier() != null) {
            Courier courier = deliveryPackage.getCourier();
            courier.setAvailable(true);
            courierRepository.save(courier);
        }

        DeliveryPackage cancelledPackage = packageRepository.save(deliveryPackage);
        webSocketService.notifyDeliveryStatusUpdate(cancelledPackage);
    }

    public List<DeliveryPackage> getCustomerPackages(String username) {
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        return packageRepository.findByCustomer(customer);
    }

    public Map<String, Object> trackPackage(Long packageId, String username) {
        DeliveryPackage deliveryPackage = getPackageById(packageId);
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (!deliveryPackage.getCustomer().equals(customer)) {
            throw new IllegalArgumentException("Package does not belong to this customer");
        }

        Map<String, Object> trackingInfo = new HashMap<>();
        
        trackingInfo.put("id", deliveryPackage.getId());
        trackingInfo.put("status", deliveryPackage.getStatus());
        trackingInfo.put("createdAt", deliveryPackage.getCreatedAt());
        trackingInfo.put("pickupAddress", deliveryPackage.getPickupAddress());
        trackingInfo.put("deliveryAddress", deliveryPackage.getDeliveryAddress());
        trackingInfo.put("description", deliveryPackage.getDescription());
        trackingInfo.put("specialInstructions", deliveryPackage.getSpecialInstructions());
        
        if (deliveryPackage.getCourier() != null) {
            Courier courier = deliveryPackage.getCourier();
            trackingInfo.put("courierName", courier.getUsername());
            trackingInfo.put("courierPhone", courier.getPhoneNumber());
        }
        
        if (deliveryPackage.getCurrentLatitude() != null && deliveryPackage.getCurrentLongitude() != null) {
            trackingInfo.put("currentLatitude", deliveryPackage.getCurrentLatitude());
            trackingInfo.put("currentLongitude", deliveryPackage.getCurrentLongitude());
            trackingInfo.put("currentLocation", deliveryPackage.getCurrentLocation());
        }
        
        trackingInfo.put("assignedAt", deliveryPackage.getAssignedAt());
        trackingInfo.put("pickedUpAt", deliveryPackage.getPickedUpAt());
        trackingInfo.put("deliveredAt", deliveryPackage.getDeliveredAt());
        trackingInfo.put("cancelledAt", deliveryPackage.getCancelledAt());
        
        return trackingInfo;
    }
}
