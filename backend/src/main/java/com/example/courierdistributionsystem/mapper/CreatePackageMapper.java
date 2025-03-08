package com.example.courierdistributionsystem.mapper;

import com.example.courierdistributionsystem.dto.CreatePackageDto;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Customer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CreatePackageMapper {

    public DeliveryPackage toEntity(CreatePackageDto dto, Customer customer) {
        if (dto == null) {
            return null;
        }

        DeliveryPackage deliveryPackage = new DeliveryPackage();
        deliveryPackage.setTrackingNumber(generateTrackingNumber());
        deliveryPackage.setCustomer(customer);
        deliveryPackage.setPickupAddress(dto.getPickupAddress());
        deliveryPackage.setDeliveryAddress(dto.getDeliveryAddress());
        deliveryPackage.setWeight(dto.getWeight());
        deliveryPackage.setDescription(dto.getDescription());
        deliveryPackage.setSpecialInstructions(dto.getSpecialInstructions());
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.PENDING);
        deliveryPackage.setCreatedAt(LocalDateTime.now());
        
        return deliveryPackage;
    }

    public CreatePackageDto toDto(DeliveryPackage entity) {
        if (entity == null) {
            return null;
        }

        CreatePackageDto dto = new CreatePackageDto();
        dto.setTrackingNumber(entity.getTrackingNumber());
        dto.setPickupAddress(entity.getPickupAddress());
        dto.setDeliveryAddress(entity.getDeliveryAddress());
        dto.setWeight(entity.getWeight());
        dto.setDescription(entity.getDescription());
        dto.setSpecialInstructions(entity.getSpecialInstructions());
        dto.setStatus(entity.getStatus());
        
        return dto;
    }

    private String generateTrackingNumber() {
        return "PKG" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
} 