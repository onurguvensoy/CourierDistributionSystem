package com.example.courierdistributionsystem.mapper;

import com.example.courierdistributionsystem.dto.DeliveryPackageDto;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import org.springframework.stereotype.Component;
import java.util.HashMap;

@Component
public class DeliveryPackageMapper {

    public DeliveryPackageDto toDto(DeliveryPackage deliveryPackage) {
        if (deliveryPackage == null) {
            return null;
        }

        DeliveryPackageDto dto = new DeliveryPackageDto();
        dto.setId(deliveryPackage.getId());
        dto.setTrackingNumber(deliveryPackage.getTrackingNumber());
        dto.setCustomerUsername(deliveryPackage.getCustomer() != null ? deliveryPackage.getCustomer().getUsername() : null);
        dto.setCourierUsername(deliveryPackage.getCourier() != null ? deliveryPackage.getCourier().getUsername() : null);
        dto.setPickupAddress(deliveryPackage.getPickupAddress());
        dto.setDeliveryAddress(deliveryPackage.getDeliveryAddress());
        dto.setWeight(deliveryPackage.getWeight());
        dto.setDescription(deliveryPackage.getDescription());
        dto.setSpecialInstructions(deliveryPackage.getSpecialInstructions());
        dto.setStatus(deliveryPackage.getStatus());
        dto.setCreatedAt(deliveryPackage.getCreatedAt());
        dto.setUpdatedAt(deliveryPackage.getUpdatedAt());

        // Map customer details
        if (deliveryPackage.getCustomer() != null) {
            HashMap<String, Object> customerDetails = new HashMap<>();
            customerDetails.put("name", deliveryPackage.getCustomer().getUsername());
            customerDetails.put("email", deliveryPackage.getCustomer().getEmail());
            customerDetails.put("phone", deliveryPackage.getCustomer().getPhoneNumber());
            dto.setCustomerDetails(customerDetails);
        }

        // Map courier details
        if (deliveryPackage.getCourier() != null) {
            HashMap<String, Object> courierDetails = new HashMap<>();
            courierDetails.put("name", deliveryPackage.getCourier().getUsername());
            courierDetails.put("email", deliveryPackage.getCourier().getEmail());
            courierDetails.put("phone", deliveryPackage.getCourier().getPhoneNumber());
            courierDetails.put("vehicleType", deliveryPackage.getCourier().getVehicleType());
            dto.setCourierDetails(courierDetails);
        }

        return dto;
    }

    public DeliveryPackage toEntity(DeliveryPackageDto dto) {
        if (dto == null) {
            return null;
        }

        DeliveryPackage deliveryPackage = new DeliveryPackage();
        deliveryPackage.setId(dto.getId());
        deliveryPackage.setTrackingNumber(dto.getTrackingNumber());
        deliveryPackage.setPickupAddress(dto.getPickupAddress());
        deliveryPackage.setDeliveryAddress(dto.getDeliveryAddress());
        deliveryPackage.setWeight(dto.getWeight());
        deliveryPackage.setDescription(dto.getDescription());
        deliveryPackage.setSpecialInstructions(dto.getSpecialInstructions());
        deliveryPackage.setStatus(dto.getStatus());
        deliveryPackage.setCreatedAt(dto.getCreatedAt());
        deliveryPackage.setUpdatedAt(dto.getUpdatedAt());
        
        return deliveryPackage;
    }
} 