package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.CreatePackageDto;
import com.example.courierdistributionsystem.dto.DeliveryPackageDto;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IDeliveryPackageService {
    DeliveryPackageDto createDeliveryPackage(CreatePackageDto packageDto);
    Optional<DeliveryPackageDto> getDeliveryPackageById(Long id);
    List<DeliveryPackageDto> getAllDeliveryPackages();
    DeliveryPackageDto updateDeliveryPackage(Long id, Map<String, String> updates);
    void deleteDeliveryPackage(Long id);
    List<DeliveryPackageDto> getCustomerActiveDeliveryPackages(String username);
    List<DeliveryPackageDto> getCustomerDeliveryHistory(String username);
    DeliveryPackageDto trackDeliveryPackage(Long packageId, String username);
    void cancelDeliveryPackage(Long packageId, String username);
    List<DeliveryPackageDto> searchDeliveryPackages(String query);
    
    // Additional methods needed by controllers
    List<DeliveryPackageDto> getAvailableDeliveryPackages();
    List<DeliveryPackageDto> getCourierActiveDeliveryPackages(String username);
    List<DeliveryPackageDto> getCustomerDeliveryPackages(String username);
    DeliveryPackageDto takeDeliveryPackage(Long packageId, String username);
    DeliveryPackageDto dropDeliveryPackage(Long packageId, String username);
    DeliveryPackageDto updateDeliveryStatus(Long packageId, String username, DeliveryPackage.DeliveryStatus status);
} 